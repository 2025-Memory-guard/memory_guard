package com.example.memory_guard.setting.dto;

import com.example.memory_guard.setting.domain.ConnectionRequest;
import com.example.memory_guard.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WardSettingsDto {

  private List<GuardianInfo> myGuardians;
  private List<RequestInfo> incomingRequests;
  private List<RequestInfo> outgoingRequests;

  @Getter
  @Builder
  public static class GuardianInfo {
    private Long guardianId;
    private String guardianUsername;

    public static GuardianInfo from(User guardian) {
      return GuardianInfo.builder()
          .guardianId(guardian.getId())
          .guardianUsername(guardian.getUserProfile().getUsername())
          .build();
    }
  }

  @Getter
  @Builder
  public static class RequestInfo {
    private Long requestId;
    private String counterpartUsername;

    public static RequestInfo fromIncoming(ConnectionRequest request) {
      return RequestInfo.builder()
          .requestId(request.getId())
          .counterpartUsername(request.getRequester().getUserProfile().getUsername())
          .build();
    }

    public static RequestInfo fromOutgoing(ConnectionRequest request) {
      return RequestInfo.builder()
          .requestId(request.getId())
          .counterpartUsername(request.getReceiver().getUserProfile().getUsername())
          .build();
    }
  }
}