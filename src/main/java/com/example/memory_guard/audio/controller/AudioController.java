package com.example.memory_guard.audio.controller;

import com.example.memory_guard.audio.dto.response.AudioAnalysisWardReport;
import com.example.memory_guard.analysis.dto.SentenceAnalysisResponseDto;
import com.example.memory_guard.analysis.dto.FinalFeedbackResponseDto;
import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.domain.AudioTranscription;
import com.example.memory_guard.audio.dto.response.*;
import com.example.memory_guard.audio.service.AudioService;
import com.example.memory_guard.analysis.service.SentenceAnalysisService;
import com.example.memory_guard.analysis.service.FinalFeedbackService;
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

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ward/audio")
public class AudioController {

  private final AudioService audioService;
  private final DiaryService diaryService;
  private final SentenceAnalysisService sentenceAnalysisService;
  private final FinalFeedbackService finalFeedbackService;
  private final int AUDIO_MIN_TIME_SECOND = 20;

  // 1. 음성저장
    @PostMapping("/save")
    public ResponseEntity<AudioSaveResponseDto> audioEvaluation(
        @RequestParam("audioFile") MultipartFile audioFile,
        @AuthenticationPrincipal User user) throws IOException, UnsupportedAudioFileException {

      if (audioFile.isEmpty()){
        throw new IllegalArgumentException("오디오가 비어있어요.");
      }

      // 테스트를 위해 꺼둠
  //    if (AudioUtils.getAudioSecondTime(audioFile) <= AUDIO_MIN_TIME_SECOND) {
  //      throw new IllegalArgumentException("오디오의 길이가 너무 짧아요. 분석을 위해 다시 녹음해주세요.");
  //    }

      // 오디오 저장
      AudioSaveResultDto saveResult = audioService.saveAudio(audioFile, user);

      // 오디오 추출
      audioService.extractAudioText(saveResult.getAudioId());

      AudioSaveResponseDto audioSaveResponseDto = AudioSaveResponseDto.builder()
          .audioId(saveResult.getAudioId())
          .consecutiveRecordingDays(saveResult.getUser().getConsecutiveRecordingDays())
          .build();

      return ResponseEntity.ok(audioSaveResponseDto);
    }

    // 따라말하기까지 끝나고 완료하기를 누르면
  @GetMapping("/report/{audioId}")
  public ResponseEntity<AudioAnalysisWardReport> audioReport(@PathVariable Long audioId, @AuthenticationPrincipal User user) throws IOException {

    AbstractAudioMetadata metadata = audioService.getAudioMetadata(audioId);

    audioService.audioEvaluate(metadata, user);

    AudioAnalysisWardReport report = audioService.audioEvaluateWardReport(metadata, user);

    return ResponseEntity.ok(report);
  }

  // 음성기록을 하고 "다음으로" 버튼을 누르면 / 테스트 확인
  @GetMapping("/{audioId}")
  public ResponseEntity<Object> getAudioAndContent(@PathVariable Long audioId, @AuthenticationPrincipal User user) throws IOException {
    AbstractAudioMetadata metadata = audioService.getAudioMetadata(audioId);

    File audioFile = metadata.getFile();
    log.info("오디오가 위치한 경로: {}", audioFile.getAbsolutePath());
    AudioTranscription audioTranscription = audioService.getAudioTranscription(audioId);
    Diary diary = diaryService.createAudioDiary(metadata, user);

    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    builder.part("title", diary.getTitle(), MediaType.APPLICATION_JSON);

    builder.part("content", createAudioTranscriptionResponseDto(audioTranscription), MediaType.APPLICATION_JSON);

    builder.part("audio", new FileSystemResource(audioFile))
        .header("Content-Disposition", "inline; filename=\"" + audioFile.getName() + "\"");

    return ResponseEntity.ok()
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(builder.build());
  }


  // 테스트 확인
  @GetMapping("/sentence/feedback/{audioId}")
  public ResponseEntity<Object> getSentenceFeedback(@PathVariable Long audioId, @AuthenticationPrincipal User user) throws IOException {
    AbstractAudioMetadata metadata = audioService.getAudioMetadata(audioId);

    SentenceAnalysisResponseDto feedbacks = sentenceAnalysisService.analyzeSentence(metadata);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(feedbacks);
  }

  // 테스트 확인
  @GetMapping("/stamps")
  public ResponseEntity<AudioStampResponseDto> getWeeklyStamps(@AuthenticationPrincipal User user) {
    AudioStampResponseDto responseDto = audioService.getAudioStamps(user);
    return ResponseEntity.ok(responseDto);
  }

  // 테스트 확인
  @GetMapping("/finalReport/{audioId}")
  public ResponseEntity<FinalFeedbackResponseDto> getFinalReport(@PathVariable Long audioId, @AuthenticationPrincipal User user) throws IOException {
    AbstractAudioMetadata metadata = audioService.getAudioMetadata(audioId);
    
    FinalFeedbackResponseDto existingFeedbacks = finalFeedbackService.getFinalFeedback(audioId);
    
    if (existingFeedbacks.getFinalFeedbacks().isEmpty()) {
      FinalFeedbackResponseDto finalFeedback = finalFeedbackService.generateAndSaveFinalFeedback(metadata);
      return ResponseEntity.ok(finalFeedback);
    } else {
      return ResponseEntity.ok(existingFeedbacks);
    }
  }

  @PostMapping("/speak/sentence")
  public ResponseEntity<SpeakSentenceResponseDto> speakSentence(
      @RequestParam("audioFile") MultipartFile audioFile,
      @RequestParam("sentence") String sentence,
      @AuthenticationPrincipal User user) throws IOException {
    
    if (audioFile.isEmpty()) {
      throw new IllegalArgumentException("오디오 파일이 비어있습니다.");
    }
    
    SpeakSentenceResponseDto result = audioService.speakSentenceProcess(audioFile, sentence);
    
    return ResponseEntity.ok(result);
  }

  private static AudioTranscriptionResponseDto createAudioTranscriptionResponseDto(AudioTranscription audioTranscription) {
    return AudioTranscriptionResponseDto.builder()
        .audioText(audioTranscription.getText())
        .audioId(audioTranscription.getAudioMetadata().getId())
        .build();
  }
}

