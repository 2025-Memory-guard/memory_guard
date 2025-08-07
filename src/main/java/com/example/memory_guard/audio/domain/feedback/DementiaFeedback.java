package com.example.memory_guard.audio.domain.feedback;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class DementiaFeedback extends AbstractEvaluationFeedback {


  @Builder
  public DementiaFeedback(User user, AbstractAudioMetadata audioMetadata) {
    super(user, audioMetadata, FeedbackType.DEMENTIA);
  }
}
