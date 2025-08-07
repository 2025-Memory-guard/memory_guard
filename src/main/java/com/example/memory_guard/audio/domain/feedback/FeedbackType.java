package com.example.memory_guard.audio.domain.feedback;

import lombok.Getter;

@Getter
public enum FeedbackType {
  DEMENTIA("치매 위험도 분석");

  private final String description;

  FeedbackType(String description) {
    this.description = description;
  }
}