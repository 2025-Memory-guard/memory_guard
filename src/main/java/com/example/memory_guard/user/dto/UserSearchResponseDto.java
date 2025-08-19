package com.example.memory_guard.user.dto;

import com.example.memory_guard.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResponseDto {

  private String userId;
  private String username;

  public static UserSearchResponseDto from(User user) {
    return UserSearchResponseDto.builder()
        .userId(user.getUserProfile().getUserId())
        .username(user.getUserProfile().getUsername())
        .build();
  }
}