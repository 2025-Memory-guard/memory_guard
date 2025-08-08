package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ward/audio")
public class AudioController {

  private final AudioService audioService;
  private final AudioMetadataRepository audioMetadataRepository;

  @PostMapping("/evaluation")
  public ResponseEntity<String> audioEvaluation(
      @RequestParam("audioFile") MultipartFile audioFile,
      @AuthenticationPrincipal User user) throws IOException {

    log.info("User가 음성 평가를 요청했습니다. {}", user);
    if (audioFile.isEmpty()) {
      throw new IllegalArgumentException("오디오 파일이 비어있습니다.");
    }

    AbstractAudioMetadata abstractAudioMetadata = audioService.saveAudio(audioFile, user);
    log.info("음성 파일이 성공적으로 저장되었습니다. {}", abstractAudioMetadata);

    //List<AbstractEvaluationFeedback> audioFeedbacks = audioService.getAudioFeedBack(abstractAudioMetadata, user);

    return ResponseEntity.ok("오디오 파일이 성공적으로 저장되었습니다.");
  }

  @GetMapping("/play/{audioId}")
  public ResponseEntity<byte[]> playAudio(@PathVariable Long audioId) throws IOException {

    File audioFile = audioService.getFile(audioId);

    byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

    HttpHeaders headers = new HttpHeaders();

    String contentType = Files.probeContentType(audioFile.toPath());
    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    headers.setContentType(MediaType.parseMediaType(contentType));
    headers.setContentLength(audioBytes.length);

    headers.setContentDispositionFormData("inline", audioFile.getName());
    return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
  }
}