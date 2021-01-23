/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Strings;

/**
 * To get an execution path that works with the provided dependencies. Will throw an exception if it is circular. Also note that the order will always be the same since each items on the same level
 * will be added in alphabetical order.
 *
 * <pre>
 * Sample usage:
 * B -&gt; C -&gt; D
 *   -&gt; E -&gt; (D)
 *        -&gt; F -&gt; G -&gt; (C)
 * H -&gt; (E)
 *
 * MultiDependenciesResolverTools resolver = new MultiDependenciesResolverTools();
 *
 * // Add some items that might be dangling
 * resolver.addItems("A", "B", "C", "D", "H");
 *
 * // Add dependencies
 * resolver.addDependency("C", "B");
 * resolver.addDependency("C", "G");
 * resolver.addDependency("D", "C");
 * resolver.addDependency("D", "E");
 * resolver.addDependency("E", "B");
 * resolver.addDependency("E", "H");
 * resolver.addDependency("F", "E");
 * resolver.addDependency("G", "F");
 *
 * List&lt;String&gt; executionPlan = resolver.getExecution();
 *
 * Gives: "A", "B", "H", "E", "F", "G", "C", "D"
 *
 * </pre>
 */
public class MultiDependenciesResolverTools {

    private Map<String, Set<String>> dependsOnByItem = new HashMap<>();

    /**
     * Add a dependency.
     *
     * @param item
     *            the item that depends on another one
     * @param dependsOnItem
     *            the item it depends on (could be null or empty to specify that it depends on nothing)
     * @return this
     */
    public MultiDependenciesResolverTools addDependency(String item, String dependsOnItem) {

        // No specified dependencies
        if (Strings.isNullOrEmpty(dependsOnItem)) {
            addItems(item);
            return this;
        }

        addItems(item, dependsOnItem);

        // Add the dependency
        dependsOnByItem.get(item).add(dependsOnItem);

        return this;
    }

    public void addItems(String... items) {
        for (String item : items) {
            if (Strings.isNullOrEmpty(item)) {
                continue;
            }
            if (!dependsOnByItem.containsKey(item)) {
                dependsOnByItem.put(item, new HashSet<>());
            }
        }
    }

    /**
     * To retrieve the execution plan.
     *
     * @return the excution plan
     * @throws SmallToolsException
     *             if it is a circular dependency
     */
    public List<String> getExecution() {

        // Clone
        Map<String, Set<String>> dependsOnByItemCopy = new HashMap<>();
        for (Entry<String, Set<String>> entry : dependsOnByItem.entrySet()) {
            dependsOnByItemCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Go through all
        List<String> plan = new ArrayList<>();
        while (!dependsOnByItemCopy.isEmpty()) {
            // Get next stage
            List<String> nextStage = new ArrayList<>();
            Iterator<Entry<String, Set<String>>> it = dependsOnByItemCopy.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Set<String>> next = it.next();
                if (next.getValue().isEmpty()) {
                    nextStage.add(next.getKey());
                    it.remove();
                }
            }
            if (nextStage.isEmpty()) {
                throw new SmallToolsException("Has a circular dependency");
            }

            // Sort and add to plan
            Collections.sort(nextStage);
            plan.addAll(nextStage);

            // Remove stage for all
            dependsOnByItemCopy.values().forEach(dependensOn -> {
                dependensOn.removeAll(nextStage);
            });

        }

        return plan;
    }

}
