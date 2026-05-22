package com.woowacourse.woochelin.chatbot;

import com.woowacourse.woochelin.common.exception.ExternalApiException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            RestClient restClient,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model
    ) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generate(String systemPrompt, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException("Gemini API 키가 설정되어 있지 않습니다. gemini.api-key를 설정해 주세요.");
        }
        try {
            GeminiResponse response = restClient.post()
                    .uri(BASE_URL + "?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GeminiRequest.of(systemPrompt, message))
                    .retrieve()
                    .body(GeminiResponse.class);
            String reply = extractText(response);
            if (reply.isBlank()) {
                throw new ExternalApiException("Gemini 응답이 비어 있습니다.");
            }
            return reply;
        } catch (ExternalApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ExternalApiException("Gemini API 호출에 실패했습니다.", exception);
        }
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return "";
        }
        Candidate candidate = response.candidates().getFirst();
        if (candidate.content() == null || candidate.content().parts() == null) {
            return "";
        }
        return candidate.content().parts().stream()
                .map(Part::text)
                .filter(text -> text != null && !text.isBlank())
                .reduce("", (left, right) -> left + right)
                .trim();
    }

    public record GeminiRequest(Content systemInstruction, List<Content> contents, GenerationConfig generationConfig) {

        static GeminiRequest of(String systemPrompt, String message) {
            return new GeminiRequest(
                    Content.system(systemPrompt),
                    List.of(Content.user(message)),
                    new GenerationConfig(0.8)
            );
        }
    }

    public record Content(String role, List<Part> parts) {

        static Content system(String text) {
            return new Content(null, List.of(new Part(text)));
        }

        static Content user(String text) {
            return new Content("user", List.of(new Part(text)));
        }
    }

    public record Part(String text) {
    }

    public record GenerationConfig(double temperature) {
    }

    public record GeminiResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }
}
