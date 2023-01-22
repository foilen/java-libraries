/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import java.util.List;

/**
 * A method to retrieve the partial or full entities.
 *
 * @param <I>
 *            the id type
 * @param <E>
 *            the partial or full entity type
 */
public interface SyncConfigurationRetrieveSlice<I, E> {

    List<E> call(SyncSlice<I> slice, long maxSliceSize);

}
