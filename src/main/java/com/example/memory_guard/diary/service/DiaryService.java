package com.example.memory_guard.diary.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.repository.DiaryRepository;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DiaryService {

  private final DiaryRepository diaryRepository;

  public Diary createAudioDiary(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
    Diary audioDiary = generateDiary(abstractAudioMetadata, user);

    diaryRepository.save(audioDiary);

    return audioDiary;
  }

  public Diary generateDiary(AbstractAudioMetadata audioMetadata, User user) throws IOException {
    File audioFile = audioMetadata.getFile();
    // AI을 활용해서 음성파일을 일기로 변환
    return new Diary("제목", "본문", user,  audioMetadata);
  }

  public Diary getDairyByAudioId(Long audioId) {
    return diaryRepository.findByAudioMetadataId(audioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 오디오 ID에 맞는 다이어리를 찾을 수 없습니다: " + audioId));
  }
}
