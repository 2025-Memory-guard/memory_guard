package com.example.memory_guard.user.dto;

import lombok.Data;

@Data
public class GuardSignupRequestDto {
  private String userId;
  private String username;
  private String password;
  private String wardUserId;
}