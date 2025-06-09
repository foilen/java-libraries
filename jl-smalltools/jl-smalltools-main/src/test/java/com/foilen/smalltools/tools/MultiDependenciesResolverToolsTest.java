package com.foilen.smalltools.tools;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.exception.SmallToolsException;

public class MultiDependenciesResolverToolsTest {

    /**
     * <pre>
     * A -&gt; B -&gt; A
     * </pre>
     */
    @Test(expected = SmallToolsException.class)
    public void testCircular() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("B", "A");
        resolver.addDependency("A", "B");

        resolver.getExecution();
    }

    /**
     * <pre>
     * A -&gt; B -&gt; C -&gt; D -&gt; A
     * E -&gt; F
     * </pre>
     */
    @Test(expected = SmallToolsException.class)
    public void testCircular2() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "C");
        resolver.addDependency("A", "D");
        resolver.addDependency("F", "E");

        resolver.getExecution();
    }

    /**
     * <pre>
     * A
     * B -&gt; C -&gt; D
     *   -&gt; E -&gt; (D)
     *        -&gt; F -&gt; G -&gt; (C)
     *             -&gt; H -&gt; (E)
     * </pre>
     */
    @Test(expected = SmallToolsException.class)
    public void testCircularComplex() {

        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        // Add some items that might be dangling
        resolver.addItems("A", "B", "C", "D", "H");

        // Add dependencies
        resolver.addDependency("C", "B");
        resolver.addDependency("C", "G");
        resolver.addDependency("D", "C");
        resolver.addDependency("D", "E");
        resolver.addDependency("E", "B");
        resolver.addDependency("E", "H");
        resolver.addDependency("F", "E");
        resolver.addDependency("G", "F");
        resolver.addDependency("H", "F");

        resolver.getExecution();
    }

    /**
     * <pre>
     * A -&gt; B -&gt; C -&gt; D -&gt; E
     * </pre>
     */
    @Test
    public void testSuccess_Single() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "C");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A
     * B -&gt; C -&gt; D
     *   -&gt; E -&gt; (D)
     *        -&gt; F -&gt; G -&gt; (C)
     * H -&gt; (E)
     * </pre>
     */
    @Test
    public void testSuccessComplex_Multi() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();

        // Add some items that might be dangling
        resolver.addItems("A", "B", "C", "D", "H");

        // Add dependencies
        resolver.addDependency("C", "B");
        resolver.addDependency("C", "G");
        resolver.addDependency("D", "C");
        resolver.addDependency("D", "E");
        resolver.addDependency("E", "B");
        resolver.addDependency("E", "H");
        resolver.addDependency("F", "E");
        resolver.addDependency("G", "F");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList( //
                "A", "B", "H", //
                "E", "F", "G", "C", "D" // strict order
        ), executionPlan);
    }

    /**
     * <pre>
     * A
     * B -&gt; C -&gt; D
     *   -&gt; E -&gt; (D)
     *        -&gt; F -&gt; G -&gt; (C)
     * H -&gt; (E)
     * </pre>
     */
    @Test
    public void testSuccessComplex_Multi_OtherOrder() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();

        // Add some items that might be dangling
        resolver.addItems("B", "A", "C", "H");

        // Add dependencies
        resolver.addDependency("G", "F");
        resolver.addDependency("D", "E");
        resolver.addDependency("F", "E");
        resolver.addDependency("C", "G");
        resolver.addDependency("E", "H");
        resolver.addDependency("C", "B");
        resolver.addDependency("E", "B");
        resolver.addDependency("D", "C");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList( //
                "A", "B", "H", //
                "E", "F", "G", "C", "D" // strict order
        ), executionPlan);
    }

    @Test
    public void testSuccessComplex_Multi_Twice() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();

        // Add some items that might be dangling
        resolver.addItems("A", "B", "C", "D", "H");

        // Add dependencies
        resolver.addDependency("C", "B");
        resolver.addDependency("C", "G");
        resolver.addDependency("D", "C");
        resolver.addDependency("D", "E");
        resolver.addDependency("E", "B");
        resolver.addDependency("E", "H");
        resolver.addDependency("F", "E");
        resolver.addDependency("G", "F");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList( //
                "A", "B", "H", //
                "E", "F", "G", "C", "D" // strict order
        ), executionPlan);

        executionPlan = resolver.getExecution();
        Assert.assertEquals(Arrays.asList( //
                "A", "B", "H", //
                "E", "F", "G", "C", "D" // strict order
        ), executionPlan);
    }

    /**
     * A,B -&gt; C
     */
    @Test
    public void testSuccessSimple_Multi() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("C", "A");
        resolver.addDependency("C", "B");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C"), executionPlan);
    }

    /**
     * <pre>
     * A -&gt; B -&gt; C -&gt; D -&gt; E
     * </pre>
     */
    @Test
    public void testSuccessWithEmpty_Single() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("A", "");
        resolver.addDependency("D", "C");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -&gt; B -&gt; C -&gt; D -&gt; E
     * </pre>
     */
    @Test
    public void testSuccessWithNull_Single() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("A", null);
        resolver.addDependency("D", "C");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -&gt; B -&gt; C -&gt; D -&gt; E
     * </pre>
     */
    @Test
    public void testSuccessWithNullAsFirst_Single() {
        MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
        resolver.addDependency("A", null);
        resolver.addDependency("E", "D");
        resolver.addDependency("B", "A");
        resolver.addDependency("C", "B");
        resolver.addDependency("D", "C");

        List<String> executionPlan = resolver.getExecution();

        Assert.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

}
