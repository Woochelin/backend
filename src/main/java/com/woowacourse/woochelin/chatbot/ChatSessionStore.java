package com.woowacourse.woochelin.chatbot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 프론트가 내려준 UUID로 대화 세션을 보관하는 인메모리 저장소.
 * LRU 방식으로 최대 {@link #MAX_SESSIONS}개만 유지해 메모리 사용량을 제한한다.
 * 서버 재시작 시 대화는 소실된다.
 */
@Component
public class ChatSessionStore {

    private static final int MAX_SESSIONS = 500;

    private final Map<String, ChatSession> sessions = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ChatSession> eldest) {
                    return size() > MAX_SESSIONS;
                }
            });

    public ChatSession getOrCreate(String sessionId, String botId, String crewName) {
        return sessions.computeIfAbsent(sessionId, key -> new ChatSession(botId, crewName));
    }
}
