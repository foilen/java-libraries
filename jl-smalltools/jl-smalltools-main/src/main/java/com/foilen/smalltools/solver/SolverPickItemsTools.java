/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.solver;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * It is a generic tool that, given a list of items, will try all the possible combination of picking a subset of them and will return these solutions. You can use it with a small list of items that
 * will return a small amount of solutions.
 *
 * <pre>
 * Sample usage:
 *
 * List&lt;Integer&gt; all = new ArrayList&lt;&gt;();
 * all.add(1);
 * all.add(2);
 * all.add(3);
 * all.add(4);
 * all.add(5);
 *
 * SolverPickItemsTools&lt;Integer&gt; solverPickItemsTools = new SolverPickItemsTools&lt;&gt;(all);
 * solverPickItemsTools.setCanReuse(false);
 *
 * // Assert sum 10 with max 3 items
 * List&lt;List&lt;Integer&gt;&gt; solutions = solverPickItemsTools.solve(possibility -&gt; {
 *
 *     int sum = possibility.stream().collect(Collectors.summingInt(it -&gt; it));
 *     if (sum == 10) {
 *         return SolveState.YES;
 *     }
 *     if (possibility.size() == 3) {
 *         return SolveState.NO_WONTBE;
 *     }
 *
 *     return SolveState.NO_ADDMORE;
 *
 * });
 *
 * List&lt;String&gt; results = solutions.stream().map(it -&gt; Joiner.on(",").join(it)).collect(Collectors.toList());
 *
 * </pre>
 *
 *
 * @param <T>
 *            item type
 *
 */
public class SolverPickItemsTools<T> {

    private List<T> items;
    private boolean canReuse = true;

    /**
     * Provide the list of items.
     *
     * @param items
     *            the items to choose from
     */
    public SolverPickItemsTools(List<T> items) {
        this.items = items;
    }

    @SuppressWarnings("unchecked")
    private void addOneAndCheck(List<List<T>> results, Function<List<T>, SolveState> solver, Deque<Integer> positions, Deque<T> possibility) {

        int nextPos = 0;
        if (!positions.isEmpty()) {
            nextPos = positions.peekLast();
            if (!canReuse) {
                ++nextPos;
                if (nextPos >= items.size()) {
                    return;
                }
            }
        }

        for (; nextPos < items.size(); ++nextPos) {
            possibility.add(items.get(nextPos));

            switch (solver.apply((List<T>) possibility)) {
            case NO_ADDMORE:
                positions.add(nextPos);
                addOneAndCheck(results, solver, positions, possibility);
                positions.removeLast();
                break;
            case NO_WONTBE:
                break;
            case YES:
                results.add(new ArrayList<>(possibility));
                break;
            }

            possibility.removeLast();
        }

    }

    /**
     * Tells if can reuse the same item multiple times in a solution.
     *
     * @return true to reuse
     */
    public boolean isCanReuse() {
        return canReuse;
    }

    /**
     * Choose if can reuse the same item multiple times in a solution.
     *
     * @param canReuse
     *            true to reuse
     */
    public void setCanReuse(boolean canReuse) {
        this.canReuse = canReuse;
    }

    /**
     * Try all the possibilities and ask the solver if it is good, if it should go deeper or if it should give up that path.
     *
     * @param solver
     *            the solver
     * @return the list of solutions
     */
    public List<List<T>> solve(Function<List<T>, SolveState> solver) {
        List<List<T>> results = new ArrayList<>();
        Deque<Integer> positions = new LinkedList<>();
        Deque<T> possibility = new LinkedList<>();
        addOneAndCheck(results, solver, positions, possibility);
        return results;
    }

}
