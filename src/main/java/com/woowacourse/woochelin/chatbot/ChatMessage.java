package com.woowacourse.woochelin.chatbot;

public record ChatMessage(Sender sender, String text) {

    public enum Sender {
        USER, BOT
    }

    public static ChatMessage user(String text) {
        return new ChatMessage(Sender.USER, text);
    }

    public static ChatMessage bot(String text) {
        return new ChatMessage(Sender.BOT, text);
    }
}
