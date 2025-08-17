package com.example.memory_guard.audio.strategy.saveStrategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AudioSaveStrategy {

  AbstractAudioMetadata save(MultipartFile audioFile, User user) throws IOException;
}
