package com.example.memory_guard.audio.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

  private final String apiKey;

  public GeminiService(@Value("${ai.api.key}") String apiKey) {
    this.apiKey = apiKey;
  }

  public String generateQuestion() {


    Client client =  Client.builder().apiKey(apiKey).build();

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash",
            "너는 40~70대와 대화하는 따뜻하고 지혜로운 친구야.\n" +
                "아래 [주제]와 관련된 일상적인 질문을 **랜덤으로 딱 1개만** 생성해 줘.\n" +
                "\n" +
                "[주제]\n" +
                "- 학창 시절, 어린 시절의 추억\n" +
                "- 소소한 일상의 즐거움이나 습관\n" +
                "- 좋아했던 음식, 노래, 장소\n" +
                "- 문득 떠오르는 생각이나 감정\n" +
                "\n" +
                "**가장 중요한 규칙:**\n" +
                "다른 어떤 인사나 설명도 붙이지 마.\n" +
                "오직 간결하고 따뜻한 질문 '한 문장'만 결과로 보여줘.\n" +
                "\n" +
                "(좋은 예시)\n" +
                "- 학창 시절, 가장 친했던 친구에 대해 이야기해 주시겠어요?\n" +
                "- 요즘 가장 마음 편해지는 시간은 언제인가요?\n" +
                "- 문득 떠오르는, 가장 그리운 맛이 있나요?\n" +
                "\n" +
                "(나쁜 예시)\n" +
                "- 안녕하세요! 질문을 하나 드릴게요. 학창 시절, 가장 친했던 친구는 누구인가요?",
            null);

    return response.text();
  }
}
