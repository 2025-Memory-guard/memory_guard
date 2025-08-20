package com.example.memory_guard.guard.dto;

import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.WardUserDto;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GuardSettingResponseDto {
    private Long id;
    private String userId;
    private String name;
    private boolean isPrimaryWard;

    public static GuardSettingResponseDto fromEntity(GuardUserLink guardUserLink, User guardian) {
        User ward = guardUserLink.getWard();
        User primaryWard = guardian.getPrimaryWard();

        boolean isPrimary = primaryWard != null && primaryWard.equals(ward);

        return GuardSettingResponseDto.builder()
            .id(ward.getId())
            .userId(ward.getUserProfile().getUserId())
            .name(ward.getUserProfile().getUsername())
            .isPrimaryWard(isPrimary)
            .build();
    }
}
