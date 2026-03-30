package com.prj.learnvocabularybe.dto.request;

public class ChatRequest {
    private String word;
    private String question;

    public ChatRequest() {}

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContent() {
        if (question != null && !question.trim().isEmpty()) {
            return question;
        }
        return word;
    }
}