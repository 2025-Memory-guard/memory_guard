package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.repository.EvaluationFeedbackRepository;
import com.example.memory_guard.audio.strategy.evaluationStrategy.AudioEvaluationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.memory_guard.user.domain.User;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioEvaluationService {
  private final List<AudioEvaluationStrategy> strategies;
  private final EvaluationFeedbackRepository feedbackRepository;

  public List<AbstractEvaluationFeedback> evaluate(AbstractAudioMetadata metadata, User user) throws IOException {
    File audioFile = metadata.getFile();
    List<AbstractEvaluationFeedback> feedbacks = new ArrayList<>();

    for (AudioEvaluationStrategy strategy : strategies) {
      log.info("음성 분석을 시작합니다. 음성 분석 모델: {}", strategy.toString());
      // AbstractEvaluationFeedback feedback = strategy.evaluate(metadata, user);
      // feedbacks.add(feedback);
    }

    feedbackRepository.saveAll(feedbacks);
    return feedbacks;
  }
}
