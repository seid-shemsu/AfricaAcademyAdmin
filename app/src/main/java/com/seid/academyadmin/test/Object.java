package com.seid.academyadmin.test;

import java.util.List;

public class Object {
    String question, answer;
    List<String> choices;

    public Object(String question, String answer, List<String> choices) {
        this.question = question;
        this.answer = answer;
        this.choices = choices;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
}
