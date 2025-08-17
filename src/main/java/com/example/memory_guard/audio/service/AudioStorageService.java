package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
import com.example.memory_guard.user.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class AudioStorageService {
  private final AudioSaveStrategy audioSaveStrategy;
  private final AudioMetadataRepository audioMetadataRepository;

  public AudioStorageService(@Qualifier("localAudioSaveStrategy") AudioSaveStrategy audioSaveStrategy,
                             AudioMetadataRepository audioMetadataRepository ){
    this.audioSaveStrategy = audioSaveStrategy;
    this.audioMetadataRepository = audioMetadataRepository;
  }

  public AbstractAudioMetadata save(MultipartFile multipartFile, User user) throws IOException {
    AbstractAudioMetadata metadata = audioSaveStrategy.save(multipartFile, user);
    return audioMetadataRepository.save(metadata);
  }

  public File getFile(Long id) throws IOException {
    AbstractAudioMetadata metaData = audioMetadataRepository.findById(id)
        .orElseThrow(() -> new IllegalStateException("존재하지 않는 Metadata입니다."));

    return metaData.getFile();
  }
}
