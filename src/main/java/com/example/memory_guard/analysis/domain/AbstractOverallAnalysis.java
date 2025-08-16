package com.example.memory_guard.analysis.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractOverallAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_metadata_id", nullable = false)
  private AbstractAudioMetadata audioMetadata;

  @Enumerated(EnumType.STRING)
  @Column(name = "feedback_type", nullable = false)
  private FeedbackType feedbackType;

  @Column(name = "score")
  private double score;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  public AbstractOverallAnalysis(AbstractAudioMetadata audioMetadata, FeedbackType feedbackType) {
    this.audioMetadata = audioMetadata;
    this.feedbackType = feedbackType;
  }

  public AbstractOverallAnalysis(AbstractAudioMetadata audioMetadata, FeedbackType feedbackType, double score) {
    this.audioMetadata = audioMetadata;
    this.feedbackType = feedbackType;
    this.score = score;
  }
}

