package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionToolsTest {

    @Test
    public void getFullStack() {
        Exception exception = new RuntimeException("aaaa", new RuntimeException("bbbb", new RuntimeException("cccc")));

        String fullStack = ExceptionTools.getFullStack(exception);
        System.out.println(fullStack);
        Assertions.assertTrue(fullStack.contains("java.lang.RuntimeException: aaaa"));
        Assertions.assertTrue(fullStack.contains("\tjava.lang.RuntimeException: bbbb"));
        Assertions.assertTrue(fullStack.contains("\t\tjava.lang.RuntimeException: cccc"));

    }

}