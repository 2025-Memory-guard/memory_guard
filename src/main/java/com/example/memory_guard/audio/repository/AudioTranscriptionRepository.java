package com.example.memory_guard.audio.repository;

import com.example.memory_guard.audio.domain.AudioTranscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioTranscriptionRepository extends JpaRepository<AudioTranscription, Long> {
  Optional<AudioTranscription> findByAudioMetadataId(Long audioMetadataId);
}
