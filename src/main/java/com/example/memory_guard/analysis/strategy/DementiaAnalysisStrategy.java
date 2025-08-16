package com.example.memory_guard.analysis.strategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
import com.example.memory_guard.analysis.domain.DementiaAnalysis;
import com.example.memory_guard.analysis.dto.OverallAnalysisResponseDto;
import com.example.memory_guard.audio.repository.AudioTranscriptionRepository;
import com.example.memory_guard.global.ai.AiModelClient;
import com.example.memory_guard.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class DementiaAnalysisStrategy implements AudioAnalysisStrategy {

  private final ObjectMapper objectMapper;
  private final AudioTranscriptionRepository audioTranscriptionRepository;
  private final AiModelClient aiModelClient;

  @Override
  public AbstractOverallAnalysis evaluate(AbstractAudioMetadata metadata, User user) throws IOException {
    OverallAnalysisResponseDto evaluateResponseDto = aiModelClient.analyzeAudio(metadata);

    log.info("AI 모델로부터 온 응답: {}", evaluateResponseDto);
    return createDementiaFeedback(metadata, evaluateResponseDto);
  }

  private static DementiaAnalysis createDementiaFeedback(AbstractAudioMetadata metadata, OverallAnalysisResponseDto evaluateResponseDto) {
    return DementiaAnalysis.builder().audioMetadata(metadata)
        .avgSilenceDuration(evaluateResponseDto.getAvgSilenceDuration())
        .fillerFrequency(evaluateResponseDto.getFillerFrequency())
        .repetitionRatio(evaluateResponseDto.getRepetitionRatio())
        .speakingRate(evaluateResponseDto.getSpeakingRate())
        .vocabularyAccuracy(evaluateResponseDto.getVocabularyAccuracy())
        .utteranceVolume(evaluateResponseDto.getUtteranceVolume())
        .score(evaluateResponseDto.getScore())
        .build();
  }
}