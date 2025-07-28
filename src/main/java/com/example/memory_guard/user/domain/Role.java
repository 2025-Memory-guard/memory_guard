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