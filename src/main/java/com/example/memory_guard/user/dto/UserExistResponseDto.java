package com.example.memory_guard.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class UserExistResponseDto {
  boolean exist;

  public UserExistResponseDto(boolean exist){
    this.exist = exist;
  }
}
