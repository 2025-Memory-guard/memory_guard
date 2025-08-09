package com.example.memory_guard.audio.dto;

public class QuestionResponseDto {
  private String question;

  public QuestionResponseDto(String question) {
    this.question = question;
  }

  public String getQuestion() {
    return question;
  }
}
