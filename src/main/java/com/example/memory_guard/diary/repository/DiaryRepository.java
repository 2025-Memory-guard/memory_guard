package com.example.memory_guard.diary.repository;

import com.example.memory_guard.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
  Optional<Diary> findByAudioMetadataId(Long audioMetadataId);
  List<Diary> findByAuthorId(Long authorId);
  List<Diary> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
  List<Diary> findByAuthorIdAndCreatedAtBetween(Long authorId, LocalDateTime start, LocalDateTime end);

  @Query("SELECT d FROM Diary d " +
      "WHERE d.author.id = :authorId " +
      "AND d.createdAt >= :startOfDay " +
      "AND d.createdAt < :endOfDay")
  List<Diary> findAllByAuthorIdAndCreatedAtToday(
      @Param("authorId") Long authorId,
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay
  );
}
