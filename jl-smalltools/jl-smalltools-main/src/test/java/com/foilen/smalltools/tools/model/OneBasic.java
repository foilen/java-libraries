/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.model;

import com.foilen.smalltools.tools.AbstractBasics;

@SuppressWarnings("unused")
public class OneBasic extends AbstractBasics {

    private String text;
    private int number;

    public OneBasic(String text, int number) {
        this.text = text;
        this.number = number;
    }

}