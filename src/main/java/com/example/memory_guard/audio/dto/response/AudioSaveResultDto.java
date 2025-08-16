package com.example.memory_guard.audio.dto.response;

import com.example.memory_guard.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioSaveResultDto {
    private Long audioId;
    private User user;
}