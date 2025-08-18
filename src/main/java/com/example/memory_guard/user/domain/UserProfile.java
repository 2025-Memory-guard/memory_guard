package com.example.memory_guard.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

  @Column(unique = true, nullable = false)
  private String userId;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Builder
  public UserProfile(String userId, String username, String password) {
    this.userId = userId;
    this.username = username;
    this.password = password;
  }
}
