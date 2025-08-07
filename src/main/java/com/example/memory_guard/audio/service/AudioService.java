package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
import com.example.memory_guard.user.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AudioService {

  private final AudioSaveStrategy audioSaveStrategy;

  public AudioService(@Qualifier("localAudioSaveStrategy")AudioSaveStrategy audioSaveStrategy) {
    this.audioSaveStrategy = audioSaveStrategy;
  }

  public AbstractAudioMetadata saveAudio(MultipartFile multipartFile, User user) throws IOException {
    return audioSaveStrategy.save(multipartFile, user);
  }
}
