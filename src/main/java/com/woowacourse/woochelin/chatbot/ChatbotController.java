package com.woowacourse.woochelin.chatbot;

import com.woowacourse.woochelin.home.RankingResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/api/v1/chatbot/trending")
    public List<RankingResponse> trending() {
        return chatbotService.trending();
    }

    @PostMapping("/api/v1/chatbot/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotService.chat(request);
    }
}
