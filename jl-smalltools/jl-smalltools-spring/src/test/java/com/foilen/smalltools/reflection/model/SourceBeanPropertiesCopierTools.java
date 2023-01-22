/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.reflection.model;

import java.util.List;

public class SourceBeanPropertiesCopierTools {
    private String text;
    private int number;
    private List<String> texts;
    private List<String> secondTexts;
    private String different;

    public String getDifferent() {
        return different;
    }

    public int getNumber() {
        return number;
    }

    public List<String> getSecondTexts() {
        return secondTexts;
    }

    public String getText() {
        return text;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setDifferent(String different) {
        this.different = different;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setSecondTexts(List<String> secondTexts) {
        this.secondTexts = secondTexts;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }
}
