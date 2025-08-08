package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.repository.AudioMetadataRepository;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.dto.DiaryResponseDto;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ward/audio")
public class AudioController {

  private final AudioService audioService;
  private final DiaryService diaryService;
  private final AudioMetadataRepository audioMetadataRepository;

  // 1. 음성저장  2. 음성평가 3. 음성일기
  @PostMapping("/evaluation")
  public ResponseEntity<String> audioEvaluation(
      @RequestParam("audioFile") MultipartFile audioFile,
      @AuthenticationPrincipal User user) throws IOException {

    if (audioFile.isEmpty()) {
      throw new IllegalArgumentException("오디오 파일이 비어있습니다.");
    }

    // 음성저장
    AbstractAudioMetadata abstractAudioMetadata = audioService.saveAudio(audioFile, user);

    // 음성평가
    //List<AbstractEvaluationFeedback> audioFeedbacks = audioService.getAudioFeedBack(abstractAudioMetadata, user);

    // 음성일기
    Diary audioDiary = diaryService.createAudioDiary(abstractAudioMetadata, user);

    log.info("음성일기 생성: {}", audioDiary);

    return ResponseEntity.ok("오디오 파일이 성공적으로 저장되었습니다.");
  }

  @GetMapping("/play/{audioId}")
  public ResponseEntity<Object> playAudio(@PathVariable Long audioId) throws IOException {

    File audioFile = audioService.getFile(audioId);
    Diary audioDiary = diaryService.getDairyByAudioId(audioId);

    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    builder.part("diary", createDiaryResponseDto(audioDiary), MediaType.APPLICATION_JSON);

    builder.part("audio", new FileSystemResource(audioFile))
        .header("Content-Disposition", "inline; filename=\"" + audioFile.getName() + "\"");

    return ResponseEntity.ok()
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(builder.build());
  }

  private static DiaryResponseDto createDiaryResponseDto(Diary audioDiary) {
    return DiaryResponseDto.builder()
        .title(audioDiary.getTitle())
        .body(audioDiary.getBody())
        .authorName(audioDiary.getAuthor().getUsername())
        .writtenAt(audioDiary.getCreatedAt().toLocalDate())
        .build();
  }
}