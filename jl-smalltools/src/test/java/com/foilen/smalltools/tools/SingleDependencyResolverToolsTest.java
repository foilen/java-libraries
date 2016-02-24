/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.exception.SmallToolsException;

public class SingleDependencyResolverToolsTest {

    /**
     * <pre>
     * A -> B -> A
     * </pre>
     */
    @Test(expected = SmallToolsException.class)
    public void testCircular() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("B", "A");
        resolver.addDependency("A", "B");

        resolver.getExecution();
    }

    /**
     * <pre>
     * A -> B -> C -> D -> A
     * E -> F
     * </pre>
     */
    @Test(expected = SmallToolsException.class)
    public void testCircular2() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "C");
        resolver.addDependency("A", "D");
        resolver.addDependency("F", "E");

        resolver.getExecution();
    }

    /**
     * <pre>
     * A -> B -> C
     *   -> D -> E
     * </pre>
     */
    @Test
    public void testSuccess() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "A");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C
     *   -> D -> E
     * </pre>
     */
    @Test
    public void testSuccessWithEmpty() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("A", "");
        resolver.addDependency("D", "A");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C
     *   -> D -> E
     * </pre>
     */
    @Test
    public void testSuccessWithNull() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("A", null);
        resolver.addDependency("D", "A");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C
     *   -> D -> E
     * </pre>
     */
    @Test
    public void testSuccessWithNullAsFirst() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("A", null);
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "A");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * A,B -> C
     */
    @Test(expected = SmallToolsException.class)
    public void testTooManyDependencies() {
        SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
        resolver.addDependency("C", "A");
        resolver.addDependency("C", "B");
    }

}
