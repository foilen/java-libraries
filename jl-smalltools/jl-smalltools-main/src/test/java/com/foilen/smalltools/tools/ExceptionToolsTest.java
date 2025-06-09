package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionToolsTest {

    @Test
    public void getFullStack() {
        Exception exception = new RuntimeException("aaaa", new RuntimeException("bbbb", new RuntimeException("cccc")));

        String fullStack = ExceptionTools.getFullStack(exception);
        System.out.println(fullStack);
        Assert.assertTrue(fullStack.contains("java.lang.RuntimeException: aaaa"));
        Assert.assertTrue(fullStack.contains("\tjava.lang.RuntimeException: bbbb"));
        Assert.assertTrue(fullStack.contains("\t\tjava.lang.RuntimeException: cccc"));

    }

}