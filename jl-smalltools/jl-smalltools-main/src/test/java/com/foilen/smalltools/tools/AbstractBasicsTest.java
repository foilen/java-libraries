package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.foilen.smalltools.tools.model.OneBasic;

public class AbstractBasicsTest {

    @Test
    public void test() {
        OneBasic a = new OneBasic("first", 1);
        OneBasic aBis = new OneBasic("first", 1);
        OneBasic b = new OneBasic("second", 2);

        Assertions.assertEquals("OneBasic[number=1,text=first]", a.toString());

        Assertions.assertTrue(a.equals(aBis));
        Assertions.assertFalse(a.equals(b));
        Assertions.assertFalse(aBis.equals(b));

        Assertions.assertEquals(a.hashCode(), aBis.hashCode());
        Assertions.assertNotEquals(b.hashCode(), aBis.hashCode());
    }

}
