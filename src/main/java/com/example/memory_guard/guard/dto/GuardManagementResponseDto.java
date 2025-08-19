package com.example.memory_guard.guard.dto;

import com.example.memory_guard.user.domain.GuardRequest;
import com.example.memory_guard.user.domain.GuardUserLink;
import com.example.memory_guard.user.dto.GuardRequestDto;
import com.example.memory_guard.user.dto.WardUserDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GuardManagementResponseDto {
    //현재 연결된 모든 피보호자
    private List<WardUserDto> allWards;
    //받은 요청 list
    private List<GuardRequestDto> allReceivedRequests;
    //보낸 요청 list
    private List<GuardRequestDto> allSentRequests;

    public static GuardManagementResponseDto fromEntity(List<GuardUserLink> allWards, List<GuardRequest> allReceivedRequests, List<GuardRequest> allSentRequests) {
        List<WardUserDto> wardDtos = allWards.stream()
                .map(WardUserDto::fromEntity)
                .toList();

        List<GuardRequestDto> receivedRequestDtos = allReceivedRequests.stream()
                .map(GuardRequestDto::fromEntity)
                .toList();

        List<GuardRequestDto> sentRequestDtos = allSentRequests.stream()
                .map(GuardRequestDto::fromEntity)
                .toList();

        return GuardManagementResponseDto.builder()
                .allWards(wardDtos)
                .allReceivedRequests(receivedRequestDtos)
                .allSentRequests(sentRequestDtos)
                .build();
    }
}
