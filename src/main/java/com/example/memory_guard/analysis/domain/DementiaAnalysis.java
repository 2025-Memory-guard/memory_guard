package com.example.memory_guard.analysis.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
public class DementiaAnalysis extends AbstractOverallAnalysis {

  private double score;

  private double dementiaProbability;

  private double speakingRate; // o

  private double utteranceVolume; // o

  private double avgSilenceDuration; // o

  private double vocabularyAccuracy; // o

  private double fillerFrequency;

  private double repetitionRatio;

  @Builder
  public DementiaAnalysis(AbstractAudioMetadata audioMetadata, double score1,
                          double speakingRate,
                          double utteranceVolume,
                          double avgSilenceDuration,
                          double vocabularyAccuracy,
                          double fillerFrequency,
                          double repetitionRatio,
                          double score, double dementiaProbability){
    super(audioMetadata, FeedbackType.DEMENTIA, score);
    this.score = score1;
    this.speakingRate = speakingRate;
    this.utteranceVolume = utteranceVolume;
    this.avgSilenceDuration = avgSilenceDuration;
    this.vocabularyAccuracy = vocabularyAccuracy;
    this.fillerFrequency = fillerFrequency;
    this.repetitionRatio = repetitionRatio;
    this.dementiaProbability = dementiaProbability;
  }
}