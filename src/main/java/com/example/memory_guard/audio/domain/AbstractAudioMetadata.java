  package com.example.memory_guard.audio.domain;

  import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
  import com.example.memory_guard.analysis.domain.SentenceAnalysisIndicators;
  import com.example.memory_guard.user.domain.User;
  import jakarta.persistence.*;
  import lombok.AccessLevel;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import org.springframework.data.annotation.CreatedDate;
  import org.springframework.data.annotation.LastModifiedDate;
  import org.springframework.data.jpa.domain.support.AuditingEntityListener;

  import java.io.File;
  import java.io.IOException;
  import java.time.LocalDateTime;
  import java.util.List;

  @Entity
  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Inheritance(strategy = InheritanceType.JOINED)
  @DiscriminatorColumn(name = "storage_type")
  @EntityListeners(AuditingEntityListener.class)
  public abstract class AbstractAudioMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration")
    private Long duration;

    @OneToMany(mappedBy = "audioMetadata")
    private List<AbstractOverallAnalysis> evaluationFeedbacks;

    @OneToMany(mappedBy = "audioMetadata")
    private List<SentenceAnalysisIndicators> sentenceAnalysisIndicators;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AbstractAudioMetadata(User user, String originalFilename, Long fileSize, Long duration) {
      this.user = user;
      this.originalFilename = originalFilename;
      this.fileSize = fileSize;
      this.duration = duration;
    }

    public void updateFileInfo(String originalFilename, Long fileSize, Long duration) {
      this.originalFilename = originalFilename;
      this.fileSize = fileSize;
      this.duration = duration;
    }

    public abstract File getFile() throws IOException;
  }
