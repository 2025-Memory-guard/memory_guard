package com.example.memory_guard.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "roles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;


  @Builder
  public Role(String name) {
    this.name = name;
  }
}



// 나의 목적은 스프링의 동작원를 얕고 넓게 이해하고 코드를 보면 어떤 식으로 동작하는지 파악할 수 있음
// 그러면 어떻게 공부해야할까?

// 스프링 공부........
