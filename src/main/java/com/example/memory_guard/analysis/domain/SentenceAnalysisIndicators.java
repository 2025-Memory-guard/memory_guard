package com.example.memory_guard.analysis.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SentenceAnalysisIndicators {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String category;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String comment;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String exampleOriginal;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String exampleSuggestion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_metadata_id", nullable = false)
  private AbstractAudioMetadata audioMetadata;
}