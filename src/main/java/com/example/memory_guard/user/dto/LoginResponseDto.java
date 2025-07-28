package com.example.memory_guard.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class LoginResponseDto {

  private String grantType;
  private String accessToken;
  private String userId;
  private List<String> roles;

  @Builder
  public LoginResponseDto(String grantType, String accessToken, String userId, List<String> roles) {
    this.grantType = grantType;
    this.accessToken = accessToken;
    this.userId = userId;
    this.roles = roles;
  }
}