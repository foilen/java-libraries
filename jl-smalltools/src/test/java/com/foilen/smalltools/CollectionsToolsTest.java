/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.CollectionsTools;

public class CollectionsToolsTest {

    @Test
    public void testRemoveValue() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assert.assertEquals(4, map.size());

        CollectionsTools.removeValue(map, 5);
        Assert.assertEquals(3, map.size());
        CollectionsTools.removeValue(map, 5);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assert.assertEquals(Integer.valueOf(1), map.get("key4"));
    }

    @Test
    public void testRemoveValues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assert.assertEquals(4, map.size());

        CollectionsTools.removeValues(map, 5);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assert.assertEquals(Integer.valueOf(1), map.get("key4"));
    }
}
