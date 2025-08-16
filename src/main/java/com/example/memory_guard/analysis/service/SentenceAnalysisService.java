package com.example.memory_guard.analysis.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.AudioTranscription;
import com.example.memory_guard.analysis.domain.SentenceAnalysisIndicators;
import com.example.memory_guard.analysis.dto.SentenceAnalysisResponseDto;
import com.example.memory_guard.analysis.dto.SentenceAnalysisIndicatorsDto;
import com.example.memory_guard.audio.repository.AudioTranscriptionRepository;
import com.example.memory_guard.analysis.repository.SentenceAnalysisRepository;
import com.example.memory_guard.global.ai.GeminiClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentenceAnalysisService {

  private final GeminiClient geminiService;
  private final AudioTranscriptionRepository audioTranscriptionRepository;
  private final SentenceAnalysisRepository sentenceAnalysisRepository;

  @Transactional
  public SentenceAnalysisResponseDto analyzeSentence(AbstractAudioMetadata audioMetadata) throws IOException {
      AudioTranscription transcription = audioTranscriptionRepository.findByAudioMetadataId(audioMetadata.getId())
          .orElseThrow(() -> new IllegalStateException("해당 음성파일에 대한 텍스트파일을 찾을 수 없습니다."));

      SentenceAnalysisIndicatorsDto sentenceAnalysisIndicators = geminiService.getLinguisticFeedback(transcription.getText());

      if (isValidAnalysisIndicator(audioMetadata, sentenceAnalysisIndicators, transcription))
        return SentenceAnalysisResponseDto.builder()
            .audioText(transcription.getText())
            .feedbacks(null)
            .build();

      List<SentenceAnalysisIndicators> feedbacks = createFeedback(audioMetadata, sentenceAnalysisIndicators);
      sentenceAnalysisRepository.saveAll(feedbacks);

      log.info("{}개의 언어적 피드백을 저장했습니다. Audio ID: {}", feedbacks.size(), audioMetadata.getId());

      return SentenceAnalysisResponseDto.builder()
          .audioText(transcription.getText())
          .feedbacks(sentenceAnalysisIndicators)
          .build();
  }

  private static boolean isValidAnalysisIndicator(AbstractAudioMetadata audioMetadata, SentenceAnalysisIndicatorsDto sentenceAnalysisIndicators, AudioTranscription transcription) {
    if (sentenceAnalysisIndicators.getLinguisticFeedback() == null || sentenceAnalysisIndicators.getLinguisticFeedback().isEmpty()) {
      log.info("언어적 피드백 항목이 발견되지 않았습니다. Audio ID: {}", audioMetadata.getId());
      return true;
    }
    return false;
  }

  private static List<SentenceAnalysisIndicators> createFeedback(AbstractAudioMetadata audioMetadata, SentenceAnalysisIndicatorsDto feedbackResponse) {
    return feedbackResponse.getLinguisticFeedback().stream()
        .map(item -> SentenceAnalysisIndicators.builder()
            .category(item.getCategory()) // [침묵], [문장흐름]...
            .comment(item.getComment()) // ______때문에 적절하지 못 하다.
            .exampleOriginal(item.getExampleOriginal()) // 이 부분이 잘 못 됐다.
            .exampleSuggestion(item.getExampleSuggestion()) // 이렇게 수정하라
            .audioMetadata(audioMetadata)
            .build())
            .collect(Collectors.toList());
  }
}
