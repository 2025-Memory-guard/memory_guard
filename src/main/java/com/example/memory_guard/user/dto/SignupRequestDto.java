package com.example.memory_guard.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignupRequestDto {
  private String userId;
  private String username;
  private String password;
}