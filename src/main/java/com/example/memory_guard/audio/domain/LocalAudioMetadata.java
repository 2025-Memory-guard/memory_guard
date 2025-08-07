package com.example.memory_guard.audio.domain;

import com.example.memory_guard.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
@Entity
@DiscriminatorValue("LOCAL")
public class LocalAudioMetadata extends AbstractAudioMetadata {

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Override
  public File getFile() throws IOException {
    File audioFile = new File(filePath);

    if (!audioFile.exists()) {
      throw new IOException("파일을 찾을 수 없습니다: " + filePath);
    }

    return audioFile;
  }

  @Builder
  public LocalAudioMetadata(User user, String originalFilename, Long fileSize, Long duration, String filePath) {
    super(user, originalFilename, fileSize, duration);
    this.filePath = filePath;
  }
}
