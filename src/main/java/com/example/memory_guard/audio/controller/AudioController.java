package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.dto.AudioStampResponseDto;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.audio.utils.AudioUtils;
import com.example.memory_guard.diary.domain.Diary;
import com.example.memory_guard.diary.dto.DiaryResponseDto;
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
  private final int AUDIO_MIN_TIME_SECOND = 20;

  // 1. 음성저장  2. 음성평가 3. 음성일기
  @PostMapping("/evaluation")
  public ResponseEntity<String> audioEvaluation(
      @RequestParam("audioFile") MultipartFile audioFile,
      @AuthenticationPrincipal User user) throws IOException {

    if (audioFile.isEmpty()){
      throw new IllegalArgumentException("오디오가 비어있어요.");
    }

    // 테스트를 위해 꺼둠
//    if (AudioUtils.getAudioSecondTime(audioFile) <= AUDIO_MIN_TIME_SECOND) {
//      throw new IllegalArgumentException("오디오의 길이가 너무 짧아요. 분석을 위해 다시 녹음해주세요.");
//    }

    audioService.processNewAudio(audioFile, user);

    return ResponseEntity.ok("오디오 파일이 성공적으로 저장되었습니다.");
  }

  @GetMapping("/play/{audioId}")
  public ResponseEntity<Object> playAudio(@PathVariable Long audioId) throws IOException {

    File audioFile = audioService.getFile(audioId);
    Diary audioDiary = audioService.getDairyByAudioId(audioId);

    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    builder.part("diary", createDiaryResponseDto(audioDiary), MediaType.APPLICATION_JSON);

    builder.part("audio", new FileSystemResource(audioFile))
        .header("Content-Disposition", "inline; filename=\"" + audioFile.getName() + "\"");

    return ResponseEntity.ok()
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(builder.build());
  }

  @GetMapping("/stamps")
  public ResponseEntity<AudioStampResponseDto> getWeeklyStamps(@AuthenticationPrincipal User user) {
    AudioStampResponseDto responseDto = audioService.getAudioStamps(user);
    return ResponseEntity.ok(responseDto);
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