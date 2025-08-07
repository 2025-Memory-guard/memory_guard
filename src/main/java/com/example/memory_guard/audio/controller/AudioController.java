package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.strategy.evaluationStrategy.AudioEvaluationStrategy;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AudioController {

  private final AudioService audioService;
  private final AudioMetadataRepository audioMetadataRepository;
//  private List<AudioEvaluationStrategy> evaluationStrategies = new ArrayList<>();


  @PostMapping("/api/ward/audio/evaluation")
  public ResponseEntity<String> audioEvaluation(
      @RequestParam("audioFile") MultipartFile audioFile,
      @AuthenticationPrincipal User user) {

    try {
      if (audioFile.isEmpty()) {
        return ResponseEntity.badRequest().body("오디오 파일이 비어있습니다.");
      }

      AbstractAudioMetadata abstractAudioMetadata = audioService.saveAudio(audioFile, user);
      abstractAudioMetadata.getAccessUri();
      // AI을 통한 분석

      return ResponseEntity.ok("오디오 파일이 성공적으로 저장되었습니다.");

    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다: " + e.getMessage());
    }
  }

}