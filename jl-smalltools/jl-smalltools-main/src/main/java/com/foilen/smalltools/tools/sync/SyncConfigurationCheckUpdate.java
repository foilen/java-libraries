/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

/**
 * A method to check if it needs to be updated.
 *
 * @param <E> the entity type
 * @param <P> the partial entity type
 */
public interface SyncConfigurationCheckUpdate<E, P> {

    /**
     * Check if the source needs to be updated to the destination.
     *
     * @param source      the source
     * @param destination the destination
     * @return true if it needs to be updated
     */
    boolean check(E source, P destination);

}
