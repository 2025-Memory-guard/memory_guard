package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.strategy.evaluationStrategy.dto.EvaluationResult;
import com.example.memory_guard.user.domain.User;

public interface AudioEvaluationStrategy {
  EvaluationResult evaluate(String filePath, User user);
}


