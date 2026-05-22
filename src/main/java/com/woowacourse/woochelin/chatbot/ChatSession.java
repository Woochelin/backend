package com.woowacourse.woochelin.chatbot;

import java.util.ArrayList;
import java.util.List;

/**
 * UUID 하나에 대응하는 단일 상담 대화. 코치(botId)와 크루 이름은 세션 생성 시 고정되고,
 * 대화 기록은 최근 {@link #MAX_HISTORY}개(질문+답변)까지만 유지한다.
 */
public class ChatSession {

    private static final int MAX_HISTORY = 20;

    private final String botId;
    private final String crewName;
    private final List<ChatMessage> history = new ArrayList<>();

    public ChatSession(String botId, String crewName) {
        this.botId = botId;
        this.crewName = crewName;
    }

    public synchronized List<ChatMessage> history() {
        return List.copyOf(history);
    }

    public synchronized void append(String userMessage, String botReply) {
        history.add(ChatMessage.user(userMessage));
        history.add(ChatMessage.bot(botReply));
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
            history.remove(0);
        }
    }

    public String botId() {
        return botId;
    }

    public String crewName() {
        return crewName;
    }
}
