package com.example.memory_guard.analysis.repository;

import com.example.memory_guard.analysis.domain.FinalFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinalFeedbackRepository extends JpaRepository<FinalFeedback, Long> {
  List<FinalFeedback> findByAudioMetadataId(Long audioMetadataId);
}