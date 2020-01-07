/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Strings;

/**
 * To get an execution path that works with the provided dependencies. Supports only one dependency per item and will throw an exception if more than one dependency are specified and if it is
 * circular.
 *
 * <pre>
 * Sample usage:
 * A -&gt; B -&gt; C
 *   -&gt; D -&gt; E
 *
 * SingleDependencyResolverTools resolver = new SingleDependencyResolverTools();
 * resolver.addDependency("E", "D");
 * resolver.addDependency("B", "A");
 * resolver.addDependency("C", "B");
 * resolver.addDependency("D", "A");
 *
 * resolver.getExecution(); // "A", "B", "C", "D", "E"
 *
 * </pre>
 */
public class SingleDependencyResolverTools {

    private class Item {
        public String name;
        public List<Item> dependedBy = new ArrayList<>();
    }

    private Map<String, Item> itemByName = new HashMap<>();
    private List<Item> roots = new ArrayList<>();
    private List<String> hasDependency = new ArrayList<>();

    /**
     * Add a dependency.
     *
     * @param item
     *            the item that depends on another one
     * @param dependsOnItem
     *            the item it depends on (could be null or empty to specify that it depends on nothing)
     * @return this
     * @throws SmallToolsException
     *             if "item" was already provided
     */
    public SingleDependencyResolverTools addDependency(String item, String dependsOnItem) {
        if (hasDependency.contains(item)) {
            throw new SmallToolsException("Item " + item + " already has one dependency");
        }
        hasDependency.add(item);

        boolean isDepending = !Strings.isNullOrEmpty(dependsOnItem);

        // Get the item
        Item currentItem = itemByName.get(item);
        if (currentItem == null) {
            currentItem = new Item();
            currentItem.name = item;
            itemByName.put(item, currentItem);
            if (!isDepending) {
                roots.add(currentItem);
            }
        } else {
            if (isDepending) {
                roots.remove(currentItem);
            }
        }

        if (isDepending) {
            // Get the item it depends on
            Item depended = itemByName.get(dependsOnItem);
            if (depended == null) {
                depended = new Item();
                depended.name = dependsOnItem;
                itemByName.put(dependsOnItem, depended);
                roots.add(depended);
            }

            // Add it
            depended.dependedBy.add(currentItem);
        }

        return this;
    }

    /**
     * To retrieve the execution plan.
     *
     * @return the excution plan
     * @throws SmallToolsException
     *             if it is a circular dependency
     */
    public List<String> getExecution() {

        List<String> executionPlan = new ArrayList<>();
        for (Item item : roots) {
            visit(executionPlan, item);
        }

        // Check if circular
        if (executionPlan.size() != itemByName.size()) {
            throw new SmallToolsException("Has a circular dependency");
        }

        return executionPlan;
    }

    private void visit(List<String> executionPlan, Item item) {
        executionPlan.add(item.name);
        for (Item child : item.dependedBy) {
            visit(executionPlan, child);
        }
    }

}
