package com.example.memory_guard.diary.domain;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Diary {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String body;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User author;

  @OneToOne
  private AbstractAudioMetadata audioMetadata;

  @CreatedDate
  private LocalDateTime createdAt;


  @Builder
  public Diary(String title, String body, User author, AbstractAudioMetadata audioMetadata){
    this.title = title;
    this.body = body;
    this.author = author;
    this.audioMetadata = audioMetadata;
  }
}
