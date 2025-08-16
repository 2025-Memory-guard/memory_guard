package com.example.memory_guard.analysis.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FinalFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_metadata_id", nullable = false)
  private AbstractAudioMetadata audioMetadata;
}