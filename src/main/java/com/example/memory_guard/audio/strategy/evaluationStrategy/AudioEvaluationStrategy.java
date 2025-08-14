package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.user.domain.User;

import java.io.File;
import java.io.IOException;

public interface AudioEvaluationStrategy {
  AbstractEvaluationFeedback evaluate(AbstractAudioMetadata metadata, User user) throws IOException;
}


