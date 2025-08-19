package com.example.memory_guard.analysis.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
import com.example.memory_guard.analysis.repository.OverallAnalysisRepository;
import com.example.memory_guard.analysis.strategy.AudioAnalysisStrategy;
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
public class AudioAnalysisService {
  private final List<AudioAnalysisStrategy> strategies;
  private final OverallAnalysisRepository feedbackRepository;

  public List<AbstractOverallAnalysis> evaluate(AbstractAudioMetadata metadata, User user) throws IOException {
    File audioFile = metadata.getFile();
    List<AbstractOverallAnalysis> feedbacks = new ArrayList<>();

    for (AudioAnalysisStrategy strategy : strategies) {
      log.info("음성 분석을 시작합니다. 음성 분석 모델: {}", strategy.toString());
      AbstractOverallAnalysis feedback = strategy.evaluate(metadata, user);
      log.info("AUDIO SCORE: {}", feedback.getScore());
      feedbacks.add(feedback);
    }

    feedbackRepository.saveAll(feedbacks);
    return feedbacks;
  }
}
