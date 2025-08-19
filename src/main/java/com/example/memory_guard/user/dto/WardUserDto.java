package com.example.memory_guard.user.dto;

import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import lombok.Setter;

@Setter
public class WardUserDto {
    private Long id;
    private String userId;
    private String name;
    private boolean isPrimary;

    public static WardUserDto fromEntity(User primaryWard) {
        WardUserDto wardUserDto = new WardUserDto();
        wardUserDto.setId(primaryWard.getId());
        wardUserDto.setUserId(primaryWard.getUserProfile().getUserId());
        wardUserDto.setName(primaryWard.getUserProfile().getUsername());
        wardUserDto.setPrimary(true);
        return wardUserDto;
    }

    public static WardUserDto fromEntity(GuardUserLink guardUserLink) {
        User ward = guardUserLink.getWard();
        WardUserDto dto = new WardUserDto();
        dto.setId(ward.getId());
        dto.setUserId(ward.getUserProfile().getUserId());
        dto.setName(ward.getUserProfile().getUsername());
        dto.setPrimary(guardUserLink.getGuardian().getPrimaryWard().equals(ward));
        return dto;
    }
}
