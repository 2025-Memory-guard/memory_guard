package com.example.memory_guard.diary.repository;

import com.example.memory_guard.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
  Optional<Diary> findByAudioMetadataId(Long audioMetadataId);
  List<Diary> findByAuthorId(Long authorId);
  List<Diary> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
