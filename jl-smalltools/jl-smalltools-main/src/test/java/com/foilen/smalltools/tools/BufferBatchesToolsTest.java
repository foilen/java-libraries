package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.google.common.base.Joiner;

public class BufferBatchesToolsTest {

    @Test
    public void testList() {
        List<String> expectedBatches = new ArrayList<>();
        expectedBatches.add("1,2,3");
        expectedBatches.add("4,5,6");
        expectedBatches.add("7,8,9");
        expectedBatches.add("10");

        List<String> batches = new ArrayList<>();

        BufferBatchesTools.<String>autoClose(3, items -> {
            batches.add(Joiner.on(",").join(items));
        }, bufferBatchesTools -> {
            bufferBatchesTools.add("1");
            bufferBatchesTools.add("2");
            bufferBatchesTools.add("3");
            bufferBatchesTools.add("4");
            bufferBatchesTools.add(Arrays.asList("5", "6", "7", "8", "9", "10"));
        });

        AssertTools.assertJsonComparison(expectedBatches, batches);

    }

    @Test
    public void testSet() {
        List<String> expectedBatches = new ArrayList<>();
        expectedBatches.add("1,2,3");
        expectedBatches.add("4,5,6");
        expectedBatches.add("7,8,9");
        expectedBatches.add("10");

        List<String> batches = new ArrayList<>();

        BufferBatchesTools.<String>autoClose(new LinkedHashSet<>(), 3, items -> {
            batches.add(Joiner.on(",").join(items));
        }, bufferBatchesTools -> {
            bufferBatchesTools.add("1");
            bufferBatchesTools.add("1");
            bufferBatchesTools.add("2");
            bufferBatchesTools.add("2");
            bufferBatchesTools.add("2");
            bufferBatchesTools.add("3");
            bufferBatchesTools.add("4");
            bufferBatchesTools.add(Arrays.asList("5", "5", "6", "7", "8", "9", "10"));
        });

        AssertTools.assertJsonComparison(expectedBatches, batches);

    }

}
