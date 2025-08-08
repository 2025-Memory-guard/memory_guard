package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class AudioService {

  private final AudioStorageService audioStorageService;
  private final AudioEvaluationService audioEvaluationService;
  private final DiaryService diaryService;

  public AudioService(AudioStorageService audioStorageService,
                      AudioEvaluationService audioEvaluationService, DiaryService diaryService) {
    this.audioStorageService = audioStorageService;
    this.audioEvaluationService = audioEvaluationService;
    this.diaryService = diaryService;
  }

  public AbstractAudioMetadata saveAudio(MultipartFile audioFile, User user) throws IOException {
    AbstractAudioMetadata metadata =  audioStorageService.save(audioFile, user);

    audioEvaluationService.evaluate(metadata, user);
    diaryService.createAudioDiary(metadata, user);

    return metadata;
  }

  public File getFile(Long audioId) throws IOException {
    return audioStorageService.getFile(audioId);
  }
}
