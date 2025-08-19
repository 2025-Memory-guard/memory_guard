package com.example.memory_guard.setting.dto;

import com.example.memory_guard.setting.domain.ConnectionRequest;
import com.example.memory_guard.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuardianSettingsDto {

  private List<WardInfo> myWards;
  private List<RequestInfo> incomingRequests;
  private List<RequestInfo> outgoingRequests;

  @Getter
  @Builder
  public static class WardInfo {
    private Long wardId;
    private String wardUsername;

    public static WardInfo from(User ward) {
      return WardInfo.builder()
          .wardId(ward.getId())
          .wardUsername(ward.getUserProfile().getUsername())
          .build();
    }
  }

  @Getter
  @Builder
  public static class RequestInfo {
    private Long requestId;
    private String requesterUsername;

    public static RequestInfo fromIncomingRequest(ConnectionRequest request) {
      return RequestInfo.builder()
          .requestId(request.getId())
          .requesterUsername(request.getRequester().getUserProfile().getUsername())
          .build();
    }

    public static RequestInfo fromOutgoingRequest(ConnectionRequest request) {
      return RequestInfo.builder()
          .requestId(request.getId())
          .requesterUsername(request.getReceiver().getUserProfile().getUsername())
          .build();
    }
  }
}