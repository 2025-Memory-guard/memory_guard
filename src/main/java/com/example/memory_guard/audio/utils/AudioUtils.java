package com.example.memory_guard.audio.utils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class AudioUtils {

    public static int getAudioSecondTimeFromFile(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        long frames = fileFormat.getFrameLength();
        float frameRate = fileFormat.getFormat().getFrameRate();

        if (frames > 0 && frameRate > 0) {
          double durationInSeconds = frames / (double) frameRate;
          return (int) Math.round(durationInSeconds);
        }
        return 0;
    }

  public static int getAudioSecondTimeFromMultipartFile(MultipartFile multipartFile) throws IOException, UnsupportedAudioFileException {
      InputStream inputStream =  new BufferedInputStream(multipartFile.getInputStream());
      AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(inputStream);
      long frames = fileFormat.getFrameLength();
      float frameRate = fileFormat.getFormat().getFrameRate();

      if (frames > 0 && frameRate > 0) {
        double durationInSeconds = frames / (double) frameRate;
        return (int) Math.round(durationInSeconds);
      }
      return 0;
  }
  }
