package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.user.domain.User;

import java.io.File;

public interface AudioEvaluationStrategy {
  AbstractEvaluationFeedback evaluate(File audioFile, User user);
}


