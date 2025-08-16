package com.example.memory_guard.diary.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.AudioTranscription;
import com.example.memory_guard.audio.repository.AudioTranscriptionRepository;
import com.example.memory_guard.global.ai.GeminiClient;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.dto.DiaryContentDto;
import com.example.memory_guard.diary.dto.DiaryResponseDto;
import com.example.memory_guard.diary.repository.DiaryRepository;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

  private final DiaryRepository diaryRepository;
  private final AudioTranscriptionRepository audioTranscriptionRepository;
  private final GeminiClient geminiService;

  public Diary createAudioDiary(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
    Diary audioDiary = generateDiary(abstractAudioMetadata, user);
    log.info("음성 일기가 생성되었습니다. {}", audioDiary);
    diaryRepository.save(audioDiary);

    return audioDiary;
  }

  public Diary generateDiary(AbstractAudioMetadata audioMetadata, User user) throws IOException {
    AudioTranscription transcription = audioTranscriptionRepository.findByAudioMetadataId(audioMetadata.getId())
        .orElseThrow(() -> new IllegalStateException("해당 오디오에 대한 텍스트 변환 데이터를 찾을 수 없습니다: " + audioMetadata.getId()));

    DiaryContentDto diaryContent = geminiService.summarizeTextToDiary(transcription.getText());

    return Diary.builder()
        .title(diaryContent.getTitle())
        .body(diaryContent.getBody())
        .author(user)
        .audioMetadata(audioMetadata)
        .build();
  }

  public Diary getDairyByAudioId(Long audioId) {
    return diaryRepository.findByAudioMetadataId(audioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 오디오 ID에 맞는 다이어리를 찾을 수 없습니다: " + audioId));
  }

  public List<DiaryResponseDto> getUserDiaries(Long userId) {
    List<Diary> diaries = diaryRepository.findByAuthorId(userId);
    return diaries.stream()
        .map(diary -> DiaryResponseDto.builder()
            .title(diary.getTitle())
            .body(diary.getBody())
            .authorName(diary.getAuthor().getUserProfile().getUsername())
            .writtenAt(diary.getCreatedAt().toLocalDate())
            .build())
        .collect(Collectors.toList());
  }
}
