package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.strategy.evaluationStrategy.dto.DementiaEvaluationResult;
import com.example.memory_guard.audio.strategy.evaluationStrategy.dto.EvaluationResult;
import com.example.memory_guard.user.domain.User;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DementiaPredictionStrategy implements AudioEvaluationStrategy{

  private final WebClient webClient;
  private final String apiUrl = "http://aimodle...";

  public DementiaPredictionStrategy(WebClient webClient) {
    this.webClient = WebClient.builder().baseUrl(apiUrl).build();
  }

  @Override
  public EvaluationResult evaluate(String filePath, User user) {
    Path path = Paths.get(filePath);
    FileSystemResource fileResource = new FileSystemResource(path);

    DementiaEvaluationResult result = webClient.post()
        .uri("요청경로")
        .contentType(MediaType.parseMediaType("audio/wav"))
        .body(BodyInserters.fromResource(fileResource))
        .retrieve()
        .bodyToMono(DementiaEvaluationResult.class)
        .block();

    return new DementiaEvaluationResult();
  }
}
