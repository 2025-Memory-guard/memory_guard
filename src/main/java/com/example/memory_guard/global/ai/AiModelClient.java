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
    File file = audioMetadata.getFile();
    String fileName = file.getName();
    String base64AudioData = encodeToBase64(file);

    log.info("AI 서버로 음성 분석 요청을 보냅니다. fileName: {}", fileName);

    AudioAnalysisRequestDto requestDto = createAudioRequestDto(base64AudioData, fileName);

    return sendHttpJsonRequest("/audio/evaluate", requestDto, OverallAnalysisResponseDto.class);
  }

  public OverallAnalysisResponseDto analyzeAudio(MultipartFile multipartFile) {
    String fileName = multipartFile.getOriginalFilename();
    String base64AudioData = encodeToBase64(multipartFile);

    log.info("AI 서버로 음성 분석 요청을 보냅니다. fileName: {}", fileName);

    AudioAnalysisRequestDto requestDto = createAudioRequestDto(base64AudioData, fileName);

    return sendHttpJsonRequest("/audio/evaluate", requestDto, OverallAnalysisResponseDto.class);
  }


  public AudioTranscriptionResponseDto extractAudioText(MultipartFile
                                                            multipartFile){
    String fileName = multipartFile.getOriginalFilename();
    String base64AudioData = encodeToBase64(multipartFile);

    log.info("AI 서버로 음성 변환 요청");

    AudioAnalysisRequestDto requestDto = createAudioRequestDto(base64AudioData, fileName);

    return sendHttpJsonRequest("/audio/translate/text", requestDto, AudioTranscriptionResponseDto.class);
  }

  public AudioTranscriptionResponseDto extractAudioText(AbstractAudioMetadata metadata) throws IOException {
    File file = metadata.getFile();
    String fileName = file.getName();
    String base64AudioData = encodeToBase64(file);

    log.info("AI 서버로 음성 변환 요청");
    log.info("fileName: {}", fileName);
    AudioAnalysisRequestDto requestDto = createAudioRequestDto(base64AudioData, fileName);

    return sendHttpJsonRequest("/audio/translate/text", requestDto, AudioTranscriptionResponseDto.class);
  }

  public SpeakSentenceResponseDto speakSentenceProcess(MultipartFile audioFile, String sentence) throws IOException {
    String tempOutputPath = System.getProperty("java.io.tmpdir") + "/" + System.currentTimeMillis() + ".wav";
    File convertedWavFile = null;

    try {
      convertedWavFile = audioConversionUtils.convertToWav(audioFile, tempOutputPath);
      String fileName = convertedWavFile.getName();
      String base64AudioData = encodeToBase64(convertedWavFile);

      log.info("AI 서버로 따라말하기 비교 요청을 보냅니다. fileName: {}, sentence: {}", fileName, sentence);

      SpeakSentenceRequestDto requestDto = createSpeakSentenceRequestDto(base64AudioData, fileName, sentence);

      return sendSpeakSentenceRequest("/audio/speak/sentence", requestDto, SpeakSentenceResponseDto.class);

    } finally {
      if (convertedWavFile != null && convertedWavFile.exists()) {
        convertedWavFile.delete();
      }
    }
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



