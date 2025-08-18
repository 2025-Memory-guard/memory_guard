package com.example.memory_guard.global.ai;

import com.example.memory_guard.audio.domain.AbstractAudioMetadata;
import com.example.memory_guard.audio.dto.request.AudioAnalysisRequestDto;
import com.example.memory_guard.audio.dto.request.SpeakSentenceRequestDto;
import com.example.memory_guard.audio.dto.response.SpeakSentenceResponseDto;
import com.example.memory_guard.audio.utils.AudioConversionUtils;
import com.example.memory_guard.analysis.dto.OverallAnalysisResponseDto;
import com.example.memory_guard.audio.dto.response.AudioTranscriptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
public class AiModelClient {

  private final WebClient webClient;
  private final AudioConversionUtils audioConversionUtils;

  public AiModelClient(@Value("${ai.api.url}") String apiUrl, AudioConversionUtils audioConversionUtils) {
    this.webClient = WebClient.builder()
        .baseUrl(apiUrl)
        .build();
    this.audioConversionUtils = audioConversionUtils;
  }

  public OverallAnalysisResponseDto analyzeAudio(AbstractAudioMetadata audioMetadata) throws IOException {
    log.info("AI 서버로 음성 분석 요청을 보냅니다 (테스트용 더미 데이터 반환)");

    OverallAnalysisResponseDto response = new OverallAnalysisResponseDto();
    response.setScore(85.5);
    response.setSpeakingRate(150.0);
    response.setUtteranceVolume(70.0);
    response.setAvgSilenceDuration(0.5);
    response.setVocabularyAccuracy(90.0);
    response.setFillerFrequency(2.0);
    response.setRepetitionRatio(5.0);
    
    return response;
  }

  public OverallAnalysisResponseDto analyzeAudio(MultipartFile multipartFile) {
    log.info("AI 서버로 음성 분석 요청을 보냅니다 (테스트용 더미 데이터 반환)");

    OverallAnalysisResponseDto response = new OverallAnalysisResponseDto();
    response.setScore(78.2);
    response.setSpeakingRate(135.0);
    response.setUtteranceVolume(65.0);
    response.setAvgSilenceDuration(0.7);
    response.setVocabularyAccuracy(88.0);
    response.setFillerFrequency(3.5);
    response.setRepetitionRatio(7.0);
    
    return response;
  }


  public AudioTranscriptionResponseDto extractAudioText(MultipartFile
                                                            multipartFile){
    log.info("AI 서버로 음성 변환 요청 (테스트용 더미 데이터 반환)");

    return AudioTranscriptionResponseDto.builder()
        .audioId(1L)
        .audioText("안녕하세요. 오늘은 날씨가 정말 좋네요. 산책하기에 딱 좋은 날이에요.")
        .build();
  }

  public AudioTranscriptionResponseDto extractAudioText(AbstractAudioMetadata metadata) throws IOException {
    log.info("AI 서버로 음성 변환 요청 (테스트용 더미 데이터 반환)");

    return AudioTranscriptionResponseDto.builder()
        .audioId(2L)
        .audioText("오늘 하루도 고생하셨습니다. 내일은 더 좋은 일이 있기를 바랍니다.")
        .build();
  }

  public SpeakSentenceResponseDto speakSentenceProcess(MultipartFile audioFile, String sentence) throws IOException {
    log.info("AI 서버로 따라말하기 비교 요청 (테스트용 더미 데이터 반환). sentence: {}", sentence);
    
    return SpeakSentenceResponseDto.builder()
        .synchronization(82.5)
        .build();
  }


  private <T> T sendHttpJsonRequest(String requestUrl, AudioAnalysisRequestDto requestDto, Class<T> responseType) {
    return webClient.post()
        .uri(requestUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(requestDto))
        .retrieve()
        .bodyToMono(responseType)
        .doOnSuccess(response -> log.info("AI 모델로부터 응답 수신 성공"))
        .doOnError(error -> log.error("AI 모델 요청 중 오류 발생", error))
        .block();
  }

  private <T> T sendSpeakSentenceRequest(String requestUrl, SpeakSentenceRequestDto requestDto, Class<T> responseType) {
    return webClient.post()
        .uri(requestUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(requestDto))
        .retrieve()
        .bodyToMono(responseType)
        .doOnSuccess(response -> log.info("AI 모델로부터 따라말하기 응답 수신 성공"))
        .doOnError(error -> log.error("AI 모델 따라말하기 요청 중 오류 발생", error))
        .block();
  }

  private String encodeToBase64(MultipartFile multipartFile) {
    try {
      byte[] fileBytes = multipartFile.getBytes();
      return Base64.getEncoder().encodeToString(fileBytes);
    } catch (IOException e) {
      throw new RuntimeException("파일 인코딩 실패", e);
    }
  }

  private String encodeToBase64(File file) {
    try {
      byte[] fileContent = Files.readAllBytes(file.toPath());
      return Base64.getEncoder().encodeToString(fileContent);
    } catch (IOException e) {
      throw new RuntimeException("파일 인코딩 실패", e);
    }
  }

  private static AudioAnalysisRequestDto createAudioRequestDto(String base64AudioData, String fileName) {
    return AudioAnalysisRequestDto.builder()
        .audioData(base64AudioData)
        .filename(fileName)
        .build();
  }

  private static SpeakSentenceRequestDto createSpeakSentenceRequestDto(String base64AudioData, String fileName, String sentence) {
    return SpeakSentenceRequestDto.builder()
        .audioData(base64AudioData)
        .filename(fileName)
        .sentence(sentence)
        .build();
  }
}



