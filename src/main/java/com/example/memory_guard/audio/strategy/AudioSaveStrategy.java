package com.example.memory_guard.audio.strategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public interface AudioSaveStrategy {

  AbstractAudioMetadata save(File audioFile, User user) throws IOException, UnsupportedAudioFileException;
}
