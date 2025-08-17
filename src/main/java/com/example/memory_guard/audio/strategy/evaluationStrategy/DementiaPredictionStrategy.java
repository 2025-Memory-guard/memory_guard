package com.example.memory_guard.audio.strategy.evaluationStrategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.domain.feedback.DementiaFeedback;
import com.example.memory_guard.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class DementiaPredictionStrategy implements AudioEvaluationStrategy{

  private final WebClient webClient;
  private final String apiUrl = "http://aimodle..."; // 터널링주소로 변경

  public DementiaPredictionStrategy() {
    this.webClient = WebClient.builder().baseUrl(apiUrl).build();
  }

  @Override
  public AbstractEvaluationFeedback evaluate(AbstractAudioMetadata metadata, User user) throws IOException {
    File audioFile = metadata.getFile();

    String response =  webClient.post()
        .uri("요청경로") // "/prediect" 요청경로 맞추기
        .contentType(MediaType.parseMediaType("audio/wav"))
        .body(BodyInserters.fromResource(new FileSystemResource(audioFile)))
        .retrieve()
        //.bodyToMono(DementiaFeedback.class)
        .bodyToMono(String.class)
        .block();

    log.info("AI 모델로부터 온 응답: {}", response);
    // builder을 이용해서 DementiaFeedback 객체만들기

    return new DementiaFeedback();

  }
}
