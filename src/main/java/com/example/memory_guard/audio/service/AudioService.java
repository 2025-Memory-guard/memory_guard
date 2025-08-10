package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.dto.AudioStampResponseDto;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AudioService {

  private final AudioStorageService audioStorageService;
  private final AudioEvaluationService audioEvaluationService;
  private final AudioMetadataRepository audioMetadataRepository;
  private final UserRepository userRepository;
  private final DiaryService diaryService;

  public AudioService(AudioStorageService audioStorageService,
                      AudioEvaluationService audioEvaluationService, AudioMetadataRepository audioMetadataRepository, UserRepository userRepository, DiaryService diaryService) {
    this.audioStorageService = audioStorageService;
    this.audioEvaluationService = audioEvaluationService;
    this.audioMetadataRepository = audioMetadataRepository;
    this.userRepository = userRepository;
    this.diaryService = diaryService;
  }

  public File getFile(Long audioId) throws IOException {
    return audioStorageService.getFile(audioId);
  }

  public AudioStampResponseDto getAudioStamps(User user) {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
    LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);

    List<AbstractAudioMetadata> weeklyRecordings = audioMetadataRepository.findByUserAndCreatedAtBetween(user, startOfWeek, endOfWeek);

    List<LocalDate> weeklyStamps = weeklyRecordings.stream()
        .map(metadata -> metadata.getCreatedAt().toLocalDate())
        .distinct()
        .sorted()
        .collect(Collectors.toList());

    return AudioStampResponseDto.builder()
        .consecutiveRecordingDays(user.getConsecutiveRecordingDays())
        .weeklyStamps(weeklyStamps)
        .build();
  }

  public List<AbstractEvaluationFeedback> evaluate(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
    return audioEvaluationService.evaluate(abstractAudioMetadata, user);
  }

  @Transactional
  public void processNewAudio(MultipartFile audioFile, User user) throws IOException {
    User persistentUser = userRepository.findById(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + user.getId()));

    // 음성 저장
    AbstractAudioMetadata abstractAudioMetadata = audioStorageService.save(audioFile, persistentUser);
    persistentUser.updateRecordingStreak();

    // 음성평가
    List<AbstractEvaluationFeedback> audioFeedbacks = audioEvaluationService.evaluate(abstractAudioMetadata, persistentUser);

    // 음성일기
    Diary audioDiary = diaryService.createAudioDiary(abstractAudioMetadata, persistentUser);
  }

  public Diary getDairyByAudioId(Long audioId) {
    return diaryService.getDairyByAudioId(audioId);
  }
}
