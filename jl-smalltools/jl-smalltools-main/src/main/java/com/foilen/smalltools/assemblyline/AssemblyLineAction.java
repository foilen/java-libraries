package com.foilen.smalltools.assemblyline;

/**
 * An action to execute of the type of item.
 *
 * @param <I> the type of item
 */
public interface AssemblyLineAction<I> {

    /**
     * Execute an action on the item. Return the object or null if it should be dropped out.
     *
     * @param item the pair to execute on
     * @return the item or null if it should be dropped out.
     */
    I executeAction(I item);

}
