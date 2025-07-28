package com.example.memory_guard.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenDto {
  private String grantType;
  private String accessToken;
  private String refreshToken;
}
