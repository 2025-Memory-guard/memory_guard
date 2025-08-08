package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.domain.feedback.DementiaFeedback;
import com.example.memory_guard.user.domain.User;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;

@Component
public class DementiaPredictionStrategy implements AudioEvaluationStrategy{

  private final WebClient webClient;
  private final String apiUrl = "http://aimodle...";

  public DementiaPredictionStrategy() {
    this.webClient = WebClient.builder().baseUrl(apiUrl).build();
  }

  @Override
  public AbstractEvaluationFeedback evaluate(File audioFile, User user) {

    return webClient.post()
        .uri("요청경로")
        .contentType(MediaType.parseMediaType("audio/wav"))
        .body(BodyInserters.fromResource(new FileSystemResource(audioFile)))
        .retrieve()
        .bodyToMono(DementiaFeedback.class)
        .block();

  }
}
