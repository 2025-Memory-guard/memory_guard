package com.example.memory_guard.audio.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AudioTranscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String text;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_metadata_id", nullable = false, unique = true)
  private AbstractAudioMetadata audioMetadata;
}
