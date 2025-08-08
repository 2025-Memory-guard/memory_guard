package com.example.memory_guard.diary.controller;

import com.example.memory_guard.diary.dto.DiaryResponseDto;
import com.example.memory_guard.diary.service.DiaryService;
import com.example.memory_guard.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ward")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/diarys")
    public ResponseEntity<List<DiaryResponseDto>> getUserDiaries(@AuthenticationPrincipal User user) {
        List<DiaryResponseDto> diaries = diaryService.getUserDiaries(user.getId());
        return ResponseEntity.ok(diaries);
    }
}