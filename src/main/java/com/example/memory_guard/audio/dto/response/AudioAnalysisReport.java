package com.example.memory_guard.audio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioAnalysisReport {

    private double speakingRate;
    private double utteranceVolume;
    private double avgSilenceDuration;
    private double vocabularyAccuracy;
    private double fillerFrequency;
    private double repetitionRatio;
    private double dementiaProbability;

    private double avgRecordingTime;
    private int attendanceRate;
    private double avgScore;
}