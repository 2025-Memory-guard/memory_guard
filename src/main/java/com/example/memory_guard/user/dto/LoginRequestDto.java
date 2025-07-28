package com.example.memory_guard.user.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
  private String userId;
  private String password;
}