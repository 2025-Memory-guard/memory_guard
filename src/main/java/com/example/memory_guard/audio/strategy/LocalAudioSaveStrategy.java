package com.example.memory_guard.audio.strategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.LocalAudioMetadata;
import com.example.memory_guard.audio.utils.AudioUtils;
import com.example.memory_guard.audio.utils.AudioConversionUtils;
import com.example.memory_guard.user.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component("localAudioSaveStrategy")
@RequiredArgsConstructor
public class LocalAudioSaveStrategy implements AudioSaveStrategy{

  @Value("${file.upload-dir}")
  private String permanentUploadDir;


  @Override
  public AbstractAudioMetadata save(File audioFile, User user) throws IOException, UnsupportedAudioFileException {
    Path uploadPath = Paths.get(permanentUploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String originalFilename = audioFile.getName();
    String uniqueFilename = UUID.randomUUID().toString() + ".wav";
    Path outputPath = uploadPath.resolve(uniqueFilename);

    try {
      Files.move(audioFile.toPath(), outputPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Failed to move file from {} to {}", audioFile.getAbsolutePath(), outputPath, e);
      throw e;
    }

    File savedFile = outputPath.toFile();
    int durationSeconds = AudioUtils.getAudioSecondTimeFromFile(savedFile);

    AbstractAudioMetadata metaData = LocalAudioMetadata.builder()
        .user(user)
        .originalFilename(originalFilename)
        .fileSize(savedFile.length())
        .duration((long) durationSeconds)
        .filePath(outputPath.toString())
        .build();

    log.info("Audio file moved and saved successfully: {}", outputPath);
    return metaData;
  }
}
