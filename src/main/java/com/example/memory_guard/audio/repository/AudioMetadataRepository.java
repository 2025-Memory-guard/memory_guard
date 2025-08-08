package com.example.memory_guard.audio.repository;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AudioMetadataRepository extends JpaRepository<AbstractAudioMetadata, Long> {

  Optional<AbstractAudioMetadata> findById(Long id);

  List<AbstractAudioMetadata> findByUser(User user);

  List<AbstractAudioMetadata> findByUserOrderByCreatedAtDesc(User user);
}