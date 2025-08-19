package com.example.memory_guard.user.dto;

import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import lombok.Builder;

@Builder
public class GuardUserDto {
    private Long id;
    private String userId;
    private String name;


    public static GuardUserDto fromEntity(User guard) {
        return GuardUserDto.builder()
                .id(guard.getId())
                .userId(guard.getUserProfile().getUserId())
                .name(guard.getUserProfile().getUsername())
                .build();
    }

    public static GuardUserDto fromEntity(GuardUserLink guardUserLink) {
        User guardian = guardUserLink.getGuardian();
        return GuardUserDto.builder()
                .id(guardian.getId())
                .userId(guardian.getUserProfile().getUserId())
                .name(guardian.getUserProfile().getUsername())
                .build();
    }
}
