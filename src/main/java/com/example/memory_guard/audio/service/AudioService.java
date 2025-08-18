package com.example.memory_guard.audio.service;

import com.example.memory_guard.analysis.service.AudioAnalysisService;
import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.AudioTranscription;
import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
import com.example.memory_guard.analysis.domain.DementiaAnalysis;
import com.example.memory_guard.analysis.domain.FeedbackType;
import com.example.memory_guard.audio.dto.response.AudioStampResponseDto;
import com.example.memory_guard.audio.dto.response.AudioAnalysisWardReport;
import com.example.memory_guard.audio.dto.response.AudioTranscriptionResponseDto;
import com.example.memory_guard.audio.dto.response.AudioSaveResultDto;
import com.example.memory_guard.audio.dto.response.SpeakSentenceResponseDto;
import com.example.memory_guard.audio.utils.AudioConversionUtils;
import com.example.memory_guard.audio.utils.AudioUtils;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.repository.AudioTranscriptionRepository;
import com.example.memory_guard.analysis.repository.OverallAnalysisRepository;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.global.ai.AiModelClient;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioService {

  private final AudioStorageService audioStorageService;
  private final AudioAnalysisService audioEvaluationService;
  private final DiaryService diaryService;
  private final AiModelClient aiModelClient;

  private final AudioMetadataRepository audioMetadataRepository;
  private final AudioTranscriptionRepository audioTranscriptionRepository;
  private final OverallAnalysisRepository evaluationFeedbackRepository;
  private final UserRepository userRepository;
  private final AudioConversionUtils audioConversionUtils;

  @Value("${file.upload-dir}")
  private String uploadDir;

  public AbstractAudioMetadata getAudioMetadata(Long audioId) throws IOException {
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

  public List<AbstractOverallAnalysis> evaluate(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
    return audioEvaluationService.evaluate(abstractAudioMetadata, user);
  }

  @Transactional
  public AudioSaveResultDto saveAudio(MultipartFile audioFile, User user) throws IOException, UnsupportedAudioFileException {
    User persistentUser = userRepository.findById(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + user.getId()));

    Path tempFilePath = Files.createTempFile("converted_", ".wav");
    File convertedWavFile = null;

    try {
      convertedWavFile = audioConversionUtils.convertToWav(audioFile, tempFilePath.toString());

      int audioTimeSeconds = AudioUtils.getAudioSecondTimeFromFile(convertedWavFile);

      AbstractAudioMetadata abstractAudioMetadata = audioStorageService.save(convertedWavFile, user);

      persistentUser.updateRecordingStreak();
      persistentUser.updateAvgRecordingTime(audioTimeSeconds);

      return AudioSaveResultDto.builder()
          .audioId(abstractAudioMetadata.getId())
          .user(persistentUser)
          .build();

    } finally {
      if (tempFilePath != null) {
        Files.deleteIfExists(tempFilePath);
      }
    }
  }

  @Transactional
  public void extractAudioText (Long audioId) throws IOException {
    AbstractAudioMetadata metadata = audioMetadataRepository
        .findById(audioId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 audio입니다."));

    AudioTranscriptionResponseDto transcriptionResponseDto = aiModelClient.extractAudioText(metadata);
    log.info("AI 모델로부터 온 데이터: {}", transcriptionResponseDto.getAudioText());
    log.info("metadata: {}", metadata);

    AudioTranscription audioTranscription = AudioTranscription.builder()
        .audioMetadata(metadata)
        .text(transcriptionResponseDto.getAudioText())
        .build();

    audioTranscriptionRepository.save(audioTranscription);
    log.info("음성 저장 및 음성 TEXT를 추출하였습니다.");
  }


  @Transactional
  public void audioEvaluate(AbstractAudioMetadata metadata, User user) throws IOException {
    List<AbstractOverallAnalysis> audioFeedbacks = audioEvaluationService.evaluate(metadata, user);
    
    // DEMENTIA 타입의 피드백에서 score를 추출하여 사용자 평균 점수 업데이트
    audioFeedbacks.stream()
        .filter(feedback -> feedback.getFeedbackType() == FeedbackType.DEMENTIA)
        .findFirst()
        .ifPresent(feedback -> {
          User persistentUser = userRepository.findById(user.getId())
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
          persistentUser.updateAvgScore(feedback.getScore());
        });
  }

  public AudioAnalysisWardReport audioEvaluateWardReport(AbstractAudioMetadata metadata, User user){
    List<AbstractOverallAnalysis> feedbacks = evaluationFeedbackRepository.findByAudioMetadataId(metadata.getId());
    
    DementiaAnalysis dementiaFeedback = feedbacks.stream()
        .filter(feedback -> feedback.getFeedbackType() == FeedbackType.DEMENTIA)
        .map(feedback -> (DementiaAnalysis) feedback)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("해당 음성에 대한 DEMENTIA 피드백을 찾을 수 없습니다."));
    
    AudioStampResponseDto audioStamps = getAudioStamps(user);
    int attendanceRate = audioStamps.getWeeklyStamps().size();
    
    return createAudioEvaluationWardReport(user, dementiaFeedback, attendanceRate);
  }

  public Diary getDairyByAudioId(Long audioId) {
    return diaryService.getDairyByAudioId(audioId);
  }

  public AudioTranscription getAudioTranscription(Long audioId) {
    return audioTranscriptionRepository.findByAudioMetadataId(audioId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 audio파일입니다."));
  }

  public SpeakSentenceResponseDto speakSentenceProcess(MultipartFile audioFile, String sentence) throws IOException {
    return aiModelClient.speakSentenceProcess(audioFile, sentence);
  }

  private static AudioAnalysisWardReport createAudioEvaluationWardReport(User user, DementiaAnalysis dementiaFeedback, int attendanceRate) {
    return AudioAnalysisWardReport.builder()
        .speakingRate(dementiaFeedback.getSpeakingRate())
        .utteranceVolume(dementiaFeedback.getUtteranceVolume())
        .avgSilenceDuration(dementiaFeedback.getAvgSilenceDuration())
        .vocabularyAccuracy(dementiaFeedback.getVocabularyAccuracy())
        .avgRecordingTime(user.getAvgRecordingTime())
        .attendanceRate(attendanceRate)
        .avgScore(user.getAvgScore())
        .build();
  }
}
