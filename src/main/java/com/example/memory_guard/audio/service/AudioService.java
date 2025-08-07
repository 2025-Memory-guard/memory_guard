package com.example.memory_guard.audio.service;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.feedback.AbstractEvaluationFeedback;
import com.example.memory_guard.audio.strategy.evaluationStrategy.AudioEvaluationStrategy;
import com.example.memory_guard.audio.strategy.saveStrategy.AudioSaveStrategy;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AudioService {

  private final AudioSaveStrategy audioSaveStrategy;
  private final List<AudioEvaluationStrategy> strategies;

  public AudioService(@Qualifier("localAudioSaveStrategy")AudioSaveStrategy audioSaveStrategy, List<AudioEvaluationStrategy> strategies) {
    this.audioSaveStrategy = audioSaveStrategy;
    this.strategies = strategies;
  }

  public AbstractAudioMetadata saveAudio(MultipartFile multipartFile, User user) throws IOException {
    return audioSaveStrategy.save(multipartFile, user);
  }

  public List<AbstractEvaluationFeedback> getAudioFeedBack(AbstractAudioMetadata abstractAudioMetadata, User user) throws IOException {
      File audioFile = abstractAudioMetadata.getFile();
      List<AbstractEvaluationFeedback> feedbacks = new ArrayList<>();

      for (AudioEvaluationStrategy strategy : strategies){
        AbstractEvaluationFeedback feedback = strategy.evaluate(audioFile, user);
        feedbacks.add(feedback);
      }

      return feedbacks;
  }
}
