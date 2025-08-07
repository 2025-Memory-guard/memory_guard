package com.example.memory_guard.audio.domain;

import com.example.memory_guard.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@DiscriminatorValue("LOCAL")
public class LocalAudioMetadata extends AbstractAudioMetadata {

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Override
  public String getAccessUri() {
    return this.filePath;
  }

  @Builder
  public LocalAudioMetadata(User user, String originalFilename, Long fileSize, Long duration, String filePath) {
    super(user, originalFilename, fileSize, duration);
    this.filePath = filePath;
  }
}
