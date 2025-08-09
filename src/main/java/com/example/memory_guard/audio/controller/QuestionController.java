package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.dto.QuestionResponseDto;
import com.example.memory_guard.audio.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

  private final GeminiService geminiService;

  @Autowired
  public QuestionController(GeminiService geminiService) {
    this.geminiService = geminiService;
  }

  @GetMapping("/today")
  public ResponseEntity<QuestionResponseDto> getTodaysQuestion() {
    String response = geminiService.generateQuestion();

    QuestionResponseDto questionResponseDto = new QuestionResponseDto(response);

    return ResponseEntity.ok(questionResponseDto);
  }
}