package com.example.memory_guard.guard.dto;

import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.WardUserDto;
import lombok.Builder;

@Builder
public class GuardSettingResponseDto {
    private String userName;
    private WardUserDto primaryWardUserDto;

    public static GuardSettingResponseDto fromEntity(User guard) {
        return GuardSettingResponseDto.builder()
                .userName(guard.getUsername())
                .primaryWardUserDto(WardUserDto.fromEntity(guard.getPrimaryWard(), true))
                .build();
    }
}
