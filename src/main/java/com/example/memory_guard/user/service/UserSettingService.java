package com.example.memory_guard.user.service;

import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.dto.GuardManagementResponseDto;
import com.example.memory_guard.user.dto.GuardUserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSettingService {

    public List<GuardUserDto> getAllGuards(User ward) {
        return ward.getGuardians().stream()
                .map(GuardUserDto::fromEntity)
                .toList();
    }

    public GuardManagementResponseDto getManagement(User ward) {
        return GuardManagementResponseDto.fromEntity(
                ward.getGuardians(),
                ward.getReceivedRequests(),
                ward.getSentRequests()
        );
    }
}
