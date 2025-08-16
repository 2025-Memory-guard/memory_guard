package com.example.memory_guard.analysis.repository;

import com.example.memory_guard.analysis.domain.AbstractOverallAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OverallAnalysisRepository extends JpaRepository<AbstractOverallAnalysis, Long> {
    List<AbstractOverallAnalysis> findByAudioMetadataId(Long audioMetadataId);
}
