package com.example.memory_guard.global.ai;

import com.example.memory_guard.analysis.dto.SentenceAnalysisIndicatorsDto;
import com.example.memory_guard.diary.dto.DiaryContentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiClient {

  private final String apiKey;
  private final ObjectMapper objectMapper;

  public GeminiClient(@Value("${ai.api.key}") String apiKey, ObjectMapper objectMapper) {
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
  }

  public SentenceAnalysisIndicatorsDto getLinguisticFeedback(String text) throws IOException {
    Client client = Client.builder().apiKey(apiKey).build();

    String prompt = "당신은 시니어의 대화 내용을 분석하여 따뜻하고 격려하는 방식으로 언어 습관에 대한 피드백을 제공하는 언어 코치입니다.\n" +
        "다음 [분석 카테고리]에 따라 주어진 텍스트를 분석하고, 반드시 [출력 규칙]을 준수하여 결과를 JSON 형식으로만 반환해야 합니다.\n\n" +
        "[분석 카테고리]\n" +
        "1. `말의 길이/흐름`: 문장 간의 연결이 자연스러운지, 이야기가 끊기지 않고 잘 이어지는지 분석합니다.\n" +
        "2. `침묵/끊김`: 대화 중 머뭇거리거나 말이 끊기는 부분이 있는지 확인합니다. (텍스트만으로는 분석이 어려우므로, '아...', '음...' 같은 표현을 단서로 활용하세요.)\n" +
        "3. `어휘 다양성`: 특정 단어(예: '조금', '정말')가 반복적으로 사용되는지 분석합니다.\n" +
        "4. `시제 일치`: 과거의 일을 이야기할 때 시제가 올바르게 사용되었는지 확인합니다. (예: '...하고 있어요' -> '...했어요')\n\n" +
        "[출력 규칙]\n" +
        "1. 응답은 `linguistic_feedback`이라는 단일 키를 가진 JSON 객체여야 합니다.\n" +
        "2. `linguistic_feedback`의 값은 피드백 객체들의 배열이어야 합니다.\n" +
        "3. 각 피드백 객체는 `category`, `comment`, `example_original`, `example_suggestion` 네 개의 키를 가져야 합니다.\n" +
        "   - `category`: [분석 카테고리] 중 하나.\n" +
        "   - `comment`: 해당 카테고리에 대한 전반적인 코멘트. 긍정적이고 격려하는 톤을 유지하세요.\n" +
        "   - `example_original`: 지적한 내용과 관련된 원본 텍스트 전부.\n" +
        "   - `example_suggestion`: 개선된 문장전부\n" +
        "4. 특정 카테고리에서 피드백할 내용이 없다면, 해당 카테고리는 결과에 포함하지 마세요.\n" +
        "5. 피드백할 내용이 전혀 없다면, 빈 배열을 값으로 반환해야 합니다. (예: `{\"linguistic_feedback\": []}`)\n" +
        "6. 다른 어떤 설명이나 인사 없이, 오직 JSON 객체만 응답해야 합니다.\n\n" +
        "[예시]\n" +
        "입력 텍스트: \"우리 초코도 밥을 잘 먹었어요. 공원까지 조금 걸었는데, 사람들이 많이 없어서 조용했어요. 점심에는 미역국을 조금 먹었고, TV를 조금 봤어요. 저녁에는 딸이랑 전화 통화를 하고 있어요.\"\n" +
        "출력 JSON:\n" +
        "{\n" +
        "  \"linguistic_feedback\": [\n" +
        "    {\n" +
        "      \"category\": \"어휘 다양성\",\n" +
        "      \"comment\": \"특정 단어가 반복되었어요. 같은 의미라도 다른 표현을 섞어보면 더 풍부한 어휘 사용이 돼요.\",\n" +
        "      \"example_original\": \"미역국을 조금 먹었고, TV를 조금 봤어요.\",\n" +
        "      \"example_suggestion\": \"‘식사로 미역국을 드셨어요’\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"category\": \"시제 일치\",\n" +
        "      \"comment\": \"과거를 표현할 때는 ‘했어요’로 표현해요.\",\n" +
        "      \"example_original\": \"전화 통화를 하고 있어요.\",\n" +
        "      \"example_suggestion\": \"‘저녁에는 딸이랑 전화 통화를 했어요'\"\n" +
        "    }\n" +
        "  ]\n" +
        "}\n\n" +
        "이제 다음 텍스트를 분석하고 피드백을 제공해 주세요 그리고 이 피드백은 최대 5개까지만 만들어줘:\n" +
        "--- 텍스트 시작 ---\n" +
        text +
        "\n--- 텍스트 끝 ---";

    GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
    String jsonResponse = response.text()
        .replace("```json", "")
        .replace("```", "")
        .trim();

    return objectMapper.readValue(jsonResponse, SentenceAnalysisIndicatorsDto.class);
  }

  public DiaryContentDto summarizeTextToDiary(String text) throws IOException {
    Client client = Client.builder().apiKey(apiKey).build();

    String prompt = "당신은 주어진 텍스트를 따뜻한 감성의 일기 형식으로 요약하는 전문가입니다.\n" +
        "다음 [규칙]에 따라 텍스트를 요약하고, 결과를 반드시 JSON 형식으로만 반환해야 합니다.\n\n" +
        "[규칙]\n" +
        "1. `title`: 텍스트의 핵심 주제를 나타내는 15자 이내의 간결한 제목이어야 합니다.\n" +
        "2. `body`: 텍스트의 주요 내용을 3개의 문장으로 요약해야 합니다. 각 문장은 줄바꿈(\\n)으로 끝나야 합니다.\n" +
        "3. 다른 설명이나 인사 없이, 오직 JSON 객체만 응답해야 합니다.\n\n" +
        "[예시]\n" +
        "입력 텍스트: \"음... 내가 어릴 때는 말이야, 동네 친구들이랑 딱지치기하고 구슬치기하고 그랬지. 저녁때 엄마가 밥 먹으라고 부를 때까지 시간 가는 줄 몰랐어. 그때 먹었던 김치찌개가 세상에서 제일 맛있었는데. 아, 그 시절이 그립네.\"\n" +
        "출력 JSON:\n" +
        "{\n" +
        "  \"title\": \"그리운 어린 시절\",\n" +
        "  \"body\": \"친구들과 해가 질 때까지 딱지치기를 하며 놀았다.\\n어머니가 끓여주시던 김치찌개 맛이 떠오른다.\\n그 시절의 소박한 행복이 문득 그리워지는 하루다.\"\n" +
        "}\n\n" +
        "이제 다음 텍스트를 요약해 주세요:\n" +
        "--- 텍스트 시작 ---\n" +
        text +
        "\n--- 텍스트 끝 ---";

    GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
    String jsonResponse = response.text()
        .replace("```json", "")
        .replace("```", "")
        .trim();

    return objectMapper.readValue(jsonResponse, DiaryContentDto.class);
  }

  public String generateQuestion() {
    Client client =  Client.builder().apiKey(apiKey).build();

    String prompt = "너는 40~70대와 대화하는 따뜻하고 지혜로운 친구야.\n" +
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
        "- 안녕하세요! 질문을 하나 드릴게요. 학창 시절, 가장 친했던 친구는 누구인가요?";

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash", prompt,null);

    return response.text();
  }

  public String generateFinalFeedback(String sentenceAnalysisText) throws IOException {
    Client client = Client.builder().apiKey(apiKey).build();

    String prompt = "당신은 시니어의 언어 분석 결과를 바탕으로 따뜻하고 격려하는 방식으로 최종 피드백을 제공하는 언어 전문가입니다.\n" +
        "주어진 문장 분석 결과들을 종합하여 다음 형식으로 피드백을 생성해주세요.\n\n" +
        "[출력 형식]\n" +
        "1. 반드시 \"오늘의 피드백:\"으로 시작하는 전반적인 피드백\n" +
        "2. 분석 결과 중에서 가장 개선이 필요한 한 가지 영역만 선택하여 구체적인 조언 제공\n\n" +
        "[예시]\n" +
        "오늘의 피드백: 전반적으로 하루의 일상을 차례대로 잘 표현해주셨어요. 특히 시간 순서에 따라 체계적으로 말씀하신 점이 인상적입니다.\n" +
        "어휘의 다양성: \"정말\"이라는 단어가 자주 사용되었네요. 다음에는 \"참으로\", \"무척\", \"매우\" 등의 다양한 표현을 사용해보시면 더 풍부한 어휘로 표현할 수 있을 거예요.\n\n" +
        "[규칙]\n" +
        "1. 격려하고 따뜻한 톤을 유지해주세요.\n" +
        "2. \"오늘의 피드백\"에서는 전반적인 평가와 긍정적인 부분을 먼저 언급해주세요.\n" +
        "3. 두 번째 피드백에서는 분석 결과 중 가장 개선이 필요한 한 가지만 선택해서 구체적인 조언을 제공해주세요.\n" +
        "4. 각 피드백은 줄바꿈으로 구분해주세요.\n" +
        "5. 총 2개의 피드백만 제공해주세요.\n\n" +
        "다음은 분석 결과입니다:\n" +
        "--- 분석 결과 시작 ---\n" +
        sentenceAnalysisText +
        "\n--- 분석 결과 끝 ---";

    GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
    return response.text().trim();
  }
}
