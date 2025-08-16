package com.example.memory_guard.analysis.service;

import com.example.memory_guard.analysis.domain.FinalFeedback;
import com.example.memory_guard.analysis.domain.SentenceAnalysisIndicators;
import com.example.memory_guard.analysis.dto.FinalFeedbackResponseDto;
import com.example.memory_guard.analysis.repository.FinalFeedbackRepository;
import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.global.ai.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinalFeedbackService {

  private final FinalFeedbackRepository finalFeedbackRepository;
  private final GeminiClient geminiClient;

  @Transactional
  public FinalFeedbackResponseDto generateAndSaveFinalFeedback(AbstractAudioMetadata audioMetadata) throws IOException {
    List<SentenceAnalysisIndicators> sentenceAnalysisIndicators = audioMetadata.getSentenceAnalysisIndicators();
    
    if (sentenceAnalysisIndicators == null || sentenceAnalysisIndicators.isEmpty()) {
      throw new IllegalArgumentException("문장 분석 결과가 없습니다.");
    }

    StringBuilder analysisText = new StringBuilder();
    for (SentenceAnalysisIndicators indicator : sentenceAnalysisIndicators) {
      analysisText.append("카테고리: ").append(indicator.getCategory()).append("\n");
      analysisText.append("코멘트: ").append(indicator.getComment()).append("\n");
      if (indicator.getExampleOriginal() != null) {
        analysisText.append("원본 예시: ").append(indicator.getExampleOriginal()).append("\n");
      }
      if (indicator.getExampleSuggestion() != null) {
        analysisText.append("개선 제안: ").append(indicator.getExampleSuggestion()).append("\n");
      }
      analysisText.append("\n");
    }

    String finalFeedbackText = geminiClient.generateFinalFeedback(analysisText.toString());
    
    List<FinalFeedback> finalFeedbacks = parseFinalFeedback(finalFeedbackText, audioMetadata);
    
    List<FinalFeedback> savedFeedbacks = finalFeedbackRepository.saveAll(finalFeedbacks);
    
    return FinalFeedbackResponseDto.from(savedFeedbacks);
  }

  public FinalFeedbackResponseDto getFinalFeedback(Long audioId) {
    List<FinalFeedback> finalFeedbacks = finalFeedbackRepository.findByAudioMetadataId(audioId);
    return FinalFeedbackResponseDto.from(finalFeedbacks);
  }

  private List<FinalFeedback> parseFinalFeedback(String feedbackText, AbstractAudioMetadata audioMetadata) {
    List<FinalFeedback> finalFeedbacks = new ArrayList<>();
    
    String[] lines = feedbackText.split("\n");
    
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;
      
      if (line.contains(":")) {
        String[] parts = line.split(":", 2);
        if (parts.length == 2) {
          String title = parts[0].trim();
          String content = parts[1].trim();
          
          FinalFeedback finalFeedback = FinalFeedback.builder()
              .title(title)
              .content(content)
              .audioMetadata(audioMetadata)
              .build();
          
          finalFeedbacks.add(finalFeedback);
        }
      }
    }
    
    return finalFeedbacks;
  }
}