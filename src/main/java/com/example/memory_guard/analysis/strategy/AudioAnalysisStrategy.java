package com.example.memory_guard.analysis.strategy;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
import com.example.memory_guard.user.domain.User;

import java.io.IOException;

public interface AudioAnalysisStrategy {
  AbstractOverallAnalysis evaluate(AbstractAudioMetadata metadata, User user) throws IOException;
}


