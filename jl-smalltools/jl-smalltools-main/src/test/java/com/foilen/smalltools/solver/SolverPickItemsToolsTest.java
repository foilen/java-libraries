package com.foilen.smalltools.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Joiner;

public class SolverPickItemsToolsTest {

    @Test
    public void testCannotReuse() {

        List<Integer> all = new ArrayList<>();
        all.add(1);
        all.add(2);
        all.add(3);
        all.add(4);
        all.add(5);

        SolverPickItemsTools<Integer> solverPickItemsTools = new SolverPickItemsTools<>(all);
        solverPickItemsTools.setCanReuse(false);

        // Assert sum 10 with max 3 items
        List<List<Integer>> solutions = solverPickItemsTools.solve(possibility -> {

            int sum = possibility.stream().collect(Collectors.summingInt(it -> it));
            if (sum == 10) {
                return SolveState.YES;
            }
            if (possibility.size() == 3) {
                return SolveState.NO_WONTBE;
            }

            return SolveState.NO_ADDMORE;

        });

        List<String> results = solutions.stream().map(it -> Joiner.on(",").join(it)).collect(Collectors.toList());
        String expected = ResourceTools.getResourceAsString("SolverPickItemsToolsTest-testCannotReuse.txt", this.getClass());
        Assert.assertEquals(expected, Joiner.on("\n").join(results));

    }

    @Test
    public void testCanReuse() {

        List<Integer> all = new ArrayList<>();
        all.add(1);
        all.add(2);
        all.add(3);
        all.add(4);
        all.add(5);

        SolverPickItemsTools<Integer> solverPickItemsTools = new SolverPickItemsTools<>(all);

        // Assert sum 10 with max 3 items
        List<List<Integer>> solutions = solverPickItemsTools.solve(possibility -> {

            int sum = possibility.stream().collect(Collectors.summingInt(it -> it));
            if (sum == 10) {
                return SolveState.YES;
            }
            if (possibility.size() == 3) {
                return SolveState.NO_WONTBE;
            }

            return SolveState.NO_ADDMORE;

        });

        List<String> results = solutions.stream().map(it -> Joiner.on(",").join(it)).collect(Collectors.toList());
        String expected = ResourceTools.getResourceAsString("SolverPickItemsToolsTest-testCanReuse.txt", this.getClass());
        Assert.assertEquals(expected, Joiner.on("\n").join(results));

    }

}
