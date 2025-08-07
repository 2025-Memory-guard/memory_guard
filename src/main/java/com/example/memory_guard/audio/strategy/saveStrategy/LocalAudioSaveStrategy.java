package com.example.memory_guard.audio.strategy.saveStrategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.user.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component("localAudioSaveStrategy")
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class LocalAudioSaveStrategy implements AudioSaveStrategy{
  private static final String UPLOAD_DIR = "src/main/resources/audio/";

  @Override
  public AbstractAudioMetadata save(MultipartFile audioFile, User user) throws IOException {
    Path uploadPath = Paths.get(UPLOAD_DIR);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String originalFilename = audioFile.getOriginalFilename();
    String extension = getExtension(originalFilename);
    String uniqueFilename = UUID.randomUUID().toString() + extension;

    Path filePath = uploadPath.resolve(uniqueFilename);
    Files.copy(audioFile.getInputStream(), filePath);

    return LocalAudioMetadata.builder()
        .user(user)
        .originalFilename(originalFilename)
        .fileSize(audioFile.getSize())
        .duration(0L)
        .filePath(filePath.toString())
        .build();
  }

  private static String getExtension(String originalFilename) {
    return originalFilename != null && originalFilename.contains(".")
        ? originalFilename.substring(originalFilename.lastIndexOf("."))
        : ".wav";
  }
}
