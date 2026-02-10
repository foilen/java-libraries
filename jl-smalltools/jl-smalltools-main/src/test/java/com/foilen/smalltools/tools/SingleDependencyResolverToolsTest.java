package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingleDependencyResolverToolsTest {

    /**
     * <pre>
     * A -> B -> A
     * </pre>
     */
    @Test
    public void testCircular() {
        assertThrows(SmallToolsException.class, () -> {
            SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
            resolver.addDependency("B", "A");
            resolver.addDependency("A", "B");

            resolver.getExecution();
        });
    }

    /**
     * <pre>
     * A -> B -> C -> D -> A
     * E -> F
     * </pre>
     */
    @Test
    public void testCircular2() {
        assertThrows(SmallToolsException.class, () -> {
            SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
            resolver.addDependency("B", "A");
            resolver.addDependency("C", "B");
            resolver.addDependency("D", "C");
            resolver.addDependency("A", "D");
            resolver.addDependency("F", "E");

            resolver.getExecution();
        });
    }

    /**
     * <pre>
     * A -> B -> C -> D -> E
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

        Assertions.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C -> D -> E
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

        Assertions.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C -> D -> E
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

        Assertions.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * <pre>
     * A -> B -> C -> D -> E
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

        Assertions.assertEquals(Arrays.asList("A", "B", "C", "D", "E"), executionPlan);
    }

    /**
     * A,B -> C
     */
    @Test
    public void testTooManyDependencies() {
        assertThrows(SmallToolsException.class, () -> {
            SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
            resolver.addDependency("C", "A");
            resolver.addDependency("C", "B");
        });
    }

}
