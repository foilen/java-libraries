package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.model.OneBasic;

public class AbstractBasicsTest {

    @Test
    public void test() {
        OneBasic a = new OneBasic("first", 1);
        OneBasic aBis = new OneBasic("first", 1);
        OneBasic b = new OneBasic("second", 2);

        Assert.assertEquals("OneBasic[number=1,text=first]", a.toString());

        Assert.assertTrue(a.equals(aBis));
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(aBis.equals(b));

        Assert.assertEquals(a.hashCode(), aBis.hashCode());
        Assert.assertNotEquals(b.hashCode(), aBis.hashCode());
    }

}
