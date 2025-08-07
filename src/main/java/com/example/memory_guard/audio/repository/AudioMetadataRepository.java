package com.example.memory_guard.audio.repository;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioMetadataRepository extends JpaRepository<AbstractAudioMetadata, Long> {

  List<AbstractAudioMetadata> findByUser(User user);

  List<AbstractAudioMetadata> findByUserOrderByCreatedAtDesc(User user);
}