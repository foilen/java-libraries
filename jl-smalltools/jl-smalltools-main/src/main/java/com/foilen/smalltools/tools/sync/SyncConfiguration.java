/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import com.foilen.smalltools.exception.SmallToolsException;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * The configuration to execute the sync. Provides mostly all the methods to execute on the different sides.
 * </p>
 *
 * <p>
 * Concepts
 * </p>
 * <ul>
 * <li>Source and destinations: they can be anything. Most likely, it will be a database and your entities would be a Map, a JPA entity or any custom objects.</li>
 * <li>You need a way to retrieve all the informations about an entity on the source that you want to copy to the destination.</li>
 * <li>You need a way to retrieve from the destination, only the id and the informations to let you decide when an entity needs to be updated.</li>
 * <li>The informations to let you decide when an entity needs to be updated could be as simple as a "version" field (when using it with JPA transactions) or just a "lastUpdated" field.</li>
 * </ul>
 *
 * <p>
 * Defaults:
 * </p>
 * <ul>
 * <li>maxSliceSize = 1000</li>
 * <li>needsUpdate returns always false (meaning only add and delete are applied)</li>
 * <li>updateHandler applies the delete and then applies the add</li>
 * </ul>
 *
 * <p>
 * Mandatories:
 * </p>
 * <ul>
 * <li>idFromEntity to retrieve the id of the provided entity</li>
 * <li>idFromPartial to retrieve the id of the provided partial entity (what you retrieve on the destination)</li>
 * <li>compareId the way to compare 2 ids</li>
 * <li>sourceSlice to retrieve the desired slice from the source (ascending ids)</li>
 * <li>destinationSlice to retrieve the desired slice from the destination (with only the id and the needed parts to know if an update is needed) (ascending ids)</li>
 * <li>addHandler to add entities to the destination</li>
 * <li>deleteHandler to delete the entities with the specified ids to the destination</li>
 * </ul>
 *
 * @param <I> the id type
 * @param <E> the entity type
 * @param <P> the partial entity type
 */
public class SyncConfiguration<I, E, P> {

    private long maxSliceSize = 1000;

    private Function<E, I> idFromEntity = (a -> {
        throw new SmallToolsException("idFromEntity not provided");
    });
    private Function<P, I> idFromPartial = (a -> {
        throw new SmallToolsException("idFromPartial not provided");
    });
    private BiFunction<I, I, Integer> compareId = ((a, b) -> {
        throw new SmallToolsException("compareId not provided");
    });

    private SyncConfigurationRetrieveSlice<I, E> sourceSlice = ((a, b) -> {
        throw new SmallToolsException("sourceSlice not provided");
    });
    private SyncConfigurationRetrieveSlice<I, P> destinationSlice = ((a, b) -> {
        throw new SmallToolsException("destinationSlice not provided");
    });

    private SyncConfigurationCheckUpdate<E, P> needsUpdate = ((a, b) -> false);

    private Consumer<List<E>> addHandler = (a -> {
        throw new SmallToolsException("addHandler not provided");
    });
    private Consumer<List<E>> updateHandler = (entities -> {
        getDeleteHandler().accept(entities.stream().map(getIdFromEntity()).collect(Collectors.toList()));
        getAddHandler().accept(entities);
    });
    private Consumer<List<I>> deleteHandler = (a -> {
        throw new SmallToolsException("deleteHandler not provided");
    });

    /**
     * The way to add entities.
     *
     * @return the handler
     */
    public Consumer<List<E>> getAddHandler() {
        return addHandler;
    }

    /**
     * The way to compare 2 ids.
     *
     * @return the handler
     */
    public BiFunction<I, I, Integer> getCompareId() {
        return compareId;
    }

    /**
     * The way to delete entities.
     *
     * @return the handler
     */
    public Consumer<List<I>> getDeleteHandler() {
        return deleteHandler;
    }

    /**
     * The way to retrieve the slice from the destination.
     *
     * @return the handler
     */
    public SyncConfigurationRetrieveSlice<I, P> getDestinationSlice() {
        return destinationSlice;
    }

    /**
     * The way to retrieve the id from the entity.
     *
     * @return the handler
     */
    public Function<E, I> getIdFromEntity() {
        return idFromEntity;
    }

    /**
     * The way to retrieve the id from the partial entity.
     *
     * @return the handler
     */
    public Function<P, I> getIdFromPartial() {
        return idFromPartial;
    }

    /**
     * The maximum size of a slice.
     *
     * @return the size
     */
    public long getMaxSliceSize() {
        return maxSliceSize;
    }

    /**
     * The way to check if an entity needs to be updated.
     *
     * @return the handler
     */
    public SyncConfigurationCheckUpdate<E, P> getNeedsUpdate() {
        return needsUpdate;
    }

    /**
     * The way to retrieve the slice from the source.
     *
     * @return the handler
     */
    public SyncConfigurationRetrieveSlice<I, E> getSourceSlice() {
        return sourceSlice;
    }

    /**
     * The way to update entities.
     *
     * @return the handler
     */
    public Consumer<List<E>> getUpdateHandler() {
        return updateHandler;
    }

    /**
     * Set the way to add entities.
     *
     * @param addHandler the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setAddHandler(Consumer<List<E>> addHandler) {
        this.addHandler = addHandler;
        return this;
    }

    /**
     * Set the way to compare 2 ids.
     *
     * @param compareId the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setCompareId(BiFunction<I, I, Integer> compareId) {
        this.compareId = compareId;
        return this;
    }

    /**
     * Set the way to delete entities.
     *
     * @param deleteHandler the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setDeleteHandler(Consumer<List<I>> deleteHandler) {
        this.deleteHandler = deleteHandler;
        return this;
    }

    /**
     * Set the way to retrieve the slice from the destination.
     *
     * @param destinationSlice the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setDestinationSlice(SyncConfigurationRetrieveSlice<I, P> destinationSlice) {
        this.destinationSlice = destinationSlice;
        return this;
    }

    /**
     * Set the way to retrieve the id from the entity.
     *
     * @param idFromEntity the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setIdFromEntity(Function<E, I> idFromEntity) {
        this.idFromEntity = idFromEntity;
        return this;
    }

    /**
     * Set the way to retrieve the id from the partial entity.
     *
     * @param idFromPartial the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setIdFromPartial(Function<P, I> idFromPartial) {
        this.idFromPartial = idFromPartial;
        return this;
    }

    /**
     * Set the maximum size of a slice.
     *
     * @param maxSliceSize the size
     * @return this
     */
    public SyncConfiguration<I, E, P> setMaxSliceSize(long maxSliceSize) {
        this.maxSliceSize = maxSliceSize;
        return this;
    }

    /**
     * Set the way to check if an entity needs to be updated.
     *
     * @param needsUpdate the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setNeedsUpdate(SyncConfigurationCheckUpdate<E, P> needsUpdate) {
        this.needsUpdate = needsUpdate;
        return this;
    }

    /**
     * Set the way to retrieve the slice from the source.
     *
     * @param sourceSlice the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setSourceSlice(SyncConfigurationRetrieveSlice<I, E> sourceSlice) {
        this.sourceSlice = sourceSlice;
        return this;
    }

    /**
     * Set the way to update entities.
     *
     * @param updateHandler the handler
     * @return this
     */
    public SyncConfiguration<I, E, P> setUpdateHandler(Consumer<List<E>> updateHandler) {
        this.updateHandler = updateHandler;
        return this;
    }

}
