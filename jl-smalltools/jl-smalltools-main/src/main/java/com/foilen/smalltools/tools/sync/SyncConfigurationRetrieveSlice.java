package com.foilen.smalltools.tools.sync;

import java.util.List;

/**
 * A method to retrieve the partial or full entities.
 *
 * @param <I> the id type
 * @param <E> the partial or full entity type
 */
public interface SyncConfigurationRetrieveSlice<I, E> {

    /**
     * Retrieve the partial or full entities.
     *
     * @param slice        the slice to retrieve
     * @param maxSliceSize the maximum size of the slice
     * @return the entities
     */
    List<E> call(SyncSlice<I> slice, long maxSliceSize);

}
