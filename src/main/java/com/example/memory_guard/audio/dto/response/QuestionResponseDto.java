package com.example.memory_guard.audio.dto.response;

public class QuestionResponseDto {
  private String question;

  public QuestionResponseDto(String question) {
    this.question = question;
  }

  public String getQuestion() {
    return question;
  }
}
