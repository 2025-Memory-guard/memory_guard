package com.example.memory_guard.analysis.dto;


import lombok.Data;

@Data
public class AiEvaluateApiResponse {
  private boolean success;
  private String message;
  private OverallAnalysisResponseDto data;
  private String error;
}