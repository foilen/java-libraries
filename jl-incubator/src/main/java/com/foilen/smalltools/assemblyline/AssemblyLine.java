/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.assemblyline;

import java.util.ArrayList;
import java.util.List;

/**
 * To execute multiple actions on one object.
 * 
 * @param <T>
 *            the type of action to execute
 * @param <I>
 *            the type of item that the actions will be applied to
 */
public class AssemblyLine<T extends AssemblyLineAction<I>, I> {

    protected List<T> actions = new ArrayList<>();

    public void addAction(T action) {
        actions.add(action);
    }

    public List<T> getActions() {
        return actions;
    }

    /**
     * Execute all the actions on the item.
     * 
     * @param item
     *            the item to process
     * @return the final item or null if it was dropped
     */
    public I process(I item) {

        if (actions == null) {
            return item;
        }

        for (T action : actions) {
            item = action.executeAction(item);
            if (item == null) {
                break;
            }
        }
        return item;
    }

    public void setActions(List<T> actions) {
        this.actions = actions;
    }

}
