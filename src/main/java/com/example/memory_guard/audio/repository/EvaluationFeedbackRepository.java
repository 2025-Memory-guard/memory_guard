package com.example.memory_guard.audio.repository;

import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationFeedbackRepository extends JpaRepository<AbstractEvaluationFeedback, Long> {
}
