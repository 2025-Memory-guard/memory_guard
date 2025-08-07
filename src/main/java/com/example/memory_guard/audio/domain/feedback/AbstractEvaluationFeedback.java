package com.example.memory_guard.audio.domain.feedback;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
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
public abstract class AbstractEvaluationFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_metadata_id", nullable = false)
  private AbstractAudioMetadata audioMetadata;

  @Enumerated(EnumType.STRING)
  @Column(name = "feedback_type", nullable = false)
  private FeedbackType feedbackType;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;


  public AbstractEvaluationFeedback(User user, AbstractAudioMetadata audioMetadata, FeedbackType feedbackType) {
    this.user = user;
    this.audioMetadata = audioMetadata;
    this.feedbackType = feedbackType;
  }
}
