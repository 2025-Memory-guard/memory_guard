package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.repository.EvaluationFeedbackRepository;
import com.example.memory_guard.audio.strategy.evaluationStrategy.AudioEvaluationStrategy;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
import com.example.memory_guard.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioService {

  private final AudioSaveStrategy audioSaveStrategy;
  private final AudioMetadataRepository audioMetadataRepository;
  private final EvaluationFeedbackRepository feedbackRepository;
  private final List<AudioEvaluationStrategy> strategies;

  public AudioService(@Qualifier("localAudioSaveStrategy")AudioSaveStrategy audioSaveStrategy,
                      AudioMetadataRepository audioMetadataRepository,
                      EvaluationFeedbackRepository feedbackRepository,
                      List<AudioEvaluationStrategy> strategies) {
    this.audioSaveStrategy = audioSaveStrategy;
    this.audioMetadataRepository = audioMetadataRepository;
    this.feedbackRepository = feedbackRepository;
    this.strategies = strategies;
  }

  public AbstractAudioMetadata saveAudio(MultipartFile multipartFile, User user) throws IOException {
    AbstractAudioMetadata metadata = audioSaveStrategy.save(multipartFile, user);
    log.info("음성파일을 저장합니다. {}", multipartFile.getOriginalFilename());

    audioMetadataRepository.save(metadata);
    return metadata;
  }

  public List<AbstractEvaluationFeedback> getAudioFeedBack(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
      File audioFile = abstractAudioMetadata.getFile();
      List<AbstractEvaluationFeedback> feedbacks = new ArrayList<>();

      for (AudioEvaluationStrategy strategy : strategies){
        log.info("음성 분석을 시작합니다. 음성 분석 모델: {}", strategy.toString());
        // 실제 분석시 사용
        //AbstractEvaluationFeedback feedback = strategy.evaluate(audioFile, user);
        //feedbacks.add(feedback);
      }

      // 피드백들 전부 저장 feedbackRepository

      return feedbacks;
  }

  public File getFile(Long id) throws IOException {
    AbstractAudioMetadata metaData = audioMetadataRepository.findById(id)
        .orElseThrow(() -> new IllegalStateException("존재하지 않는 Metadata입니다."));

    return metaData.getFile();
  }
}
