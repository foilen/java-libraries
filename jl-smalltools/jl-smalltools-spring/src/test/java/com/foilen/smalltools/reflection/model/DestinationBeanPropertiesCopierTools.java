/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.reflection.model;

import java.util.Set;

public class DestinationBeanPropertiesCopierTools {

    private String text;
    private String secondText;
    private int number;
    private int secondNumber;
    private Set<String> texts;
    private Set<String> secondTexts;
    private Long different;

    public Long getDifferent() {
        return different;
    }

    public int getNumber() {
        return number;
    }

    public int getSecondNumber() {
        return secondNumber;
    }

    public String getSecondText() {
        return secondText;
    }

    public Set<String> getSecondTexts() {
        return secondTexts;
    }

    public String getText() {
        return text;
    }

    public Set<String> getTexts() {
        return texts;
    }

    public void setDifferent(Long different) {
        this.different = different;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setSecondNumber(int secondNumber) {
        this.secondNumber = secondNumber;
    }

    public void setSecondText(String secondText) {
        this.secondText = secondText;
    }

    public void setSecondTexts(Set<String> secondTexts) {
        this.secondTexts = secondTexts;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTexts(Set<String> texts) {
        this.texts = texts;
    }

}
