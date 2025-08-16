package com.example.memory_guard.audio.utils;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class AudioConversionUtils {

    public File convertToWav(MultipartFile audioFile, String outputPath) throws IOException {
        FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // 임시 파일 생성
        Path tempInputFile = Files.createTempFile("temp_audio_", getOriginalExtension(audioFile.getOriginalFilename()));
        audioFile.transferTo(tempInputFile.toFile());

        // WAV 출력 파일 경로
        Path outputFilePath = Paths.get(outputPath);
        
        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(tempInputFile.toString())
                    .overrideOutputFiles(true)
                    .addOutput(outputFilePath.toString())
                    .setFormat("wav")
                    .setAudioCodec("pcm_s16le")
                    .setAudioSampleRate(44100)
                    .setAudioChannels(1)
                    .done();

            executor.createJob(builder).run();
            log.info("Audio conversion completed: {} -> {}", tempInputFile, outputFilePath);
            
            return outputFilePath.toFile();
        } finally {
            Files.deleteIfExists(tempInputFile);
        }
    }

    private String getOriginalExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".m4a";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}