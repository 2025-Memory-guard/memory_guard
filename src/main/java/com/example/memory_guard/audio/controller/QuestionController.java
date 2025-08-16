package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.dto.response.QuestionResponseDto;
import com.example.memory_guard.global.ai.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ward/audio")
public class QuestionController {

  private final GeminiClient geminiService;

  @Autowired
  public QuestionController(GeminiClient geminiService) {
    this.geminiService = geminiService;
  }

  @GetMapping("/question")
  public ResponseEntity<QuestionResponseDto> getTodaysQuestion() {
    String response = geminiService.generateQuestion();

    QuestionResponseDto questionResponseDto = new QuestionResponseDto(response);

    return ResponseEntity.ok(questionResponseDto);
  }
}