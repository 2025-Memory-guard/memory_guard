package com.example.memory_guard.audio.utils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class AudioUtils {

  public static int getAudioSecondTime(MultipartFile multipartFile) {
    File tempFile = null;
    try {
      tempFile = File.createTempFile("temp-audio", "." + getFileExtension(multipartFile.getOriginalFilename()));
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write(multipartFile.getBytes());
      }

      AudioFile audioFile = AudioFileIO.read(tempFile);

      AudioHeader audioHeader = audioFile.getAudioHeader();
      if (audioHeader != null) {
        System.out.println("audio length: " + audioHeader.getTrackLength());
        return audioHeader.getTrackLength();
      }
      return 0;

    } catch (Exception e) {
      throw new RuntimeException("오디오 파일 길이를 측정 중 오류가 발생했습니다.", e);
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
  }

  private static String getFileExtension(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "";
    }
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
  }
}

