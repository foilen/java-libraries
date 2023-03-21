/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.assemblyline;

import java.util.ArrayList;
import java.util.List;

/**
 * To execute multiple actions on one object.
 *
 * @param <I> the type of item that the actions will be applied to
 */
public class AssemblyLine<T extends AssemblyLineAction<I>, I> {

    /**
     * The actions to execute.
     */
    protected List<AssemblyLineAction<I>> actions = new ArrayList<>();

    /**
     * Add an action to the list.
     *
     * @param action the action to add
     */
    public void addAction(T action) {
        actions.add(action);
    }

    /**
     * Get the actions.
     *
     * @return the actions
     */
    public List<AssemblyLineAction<I>> getActions() {
        return actions;
    }

    /**
     * Execute all the actions on the item.
     *
     * @param item the item to process
     * @return the final item or null if it was dropped
     */
    public I process(I item) {

        if (actions == null) {
            return item;
        }

        for (AssemblyLineAction<I> action : actions) {
            item = action.executeAction(item);
            if (item == null) {
                break;
            }
        }
        return item;
    }

    /**
     * Set the actions.
     *
     * @param actions the actions
     */
    public void setActions(List<AssemblyLineAction<I>> actions) {
        this.actions = actions;
    }

}
