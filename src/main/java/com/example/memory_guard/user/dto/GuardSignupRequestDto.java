package com.example.memory_guard.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuardSignupRequestDto extends SignupRequestDto {
  private String wardUserId;
}