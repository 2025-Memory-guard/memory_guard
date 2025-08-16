package com.example.memory_guard.analysis.repository;

import com.example.memory_guard.analysis.domain.SentenceAnalysisIndicators;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceAnalysisRepository extends JpaRepository<SentenceAnalysisIndicators, Long> {
}
