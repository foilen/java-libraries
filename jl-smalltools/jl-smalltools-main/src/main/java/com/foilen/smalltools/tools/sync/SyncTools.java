/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.tuple.Tuple2;

/**
 * To help sync 2 datastores by adding entities that are not in the destination (per id), delete extra entities in the destination (per id) and possibly update the destination if you consider it
 * changed (per id and your defined subset to check for change). <br/>
 * <br/>
 *
 * See {@link SyncConfiguration} for more detail.
 *
 * <pre>
 * Dependencies:
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
public final class SyncTools {

    private final static Logger log = LoggerFactory.getLogger(SyncTools.class);

    private static <E, I, P> I findFrom(SyncConfiguration<I, E, P> syncConfiguration, SyncSlice<I> syncSlice, List<E> sourceEntities, List<P> destinationEntities) {

        I from = null;

        if (!sourceEntities.isEmpty()) {
            if (syncSlice.getAfterId().isPresent()) {
                Optional<I> first = sourceEntities.stream() //
                        .map(it -> syncConfiguration.getIdFromEntity().apply(it)) //
                        .filter(it -> syncConfiguration.getCompareId().apply(it, syncSlice.getAfterId().get()) > 0) //
                        .findFirst();
                if (first.isPresent()) {
                    from = first.get();
                }
            } else {
                from = syncConfiguration.getIdFromEntity().apply(sourceEntities.get(0));
            }
        }

        if (!destinationEntities.isEmpty()) {

            I finalFrom = from;

            Optional<I> first = destinationEntities.stream() //
                    .map(it -> syncConfiguration.getIdFromPartial().apply(it)) //
                    .filter(it -> !syncSlice.getAfterId().isPresent() || syncConfiguration.getCompareId().apply(it, syncSlice.getAfterId().get()) > 0) //
                    .filter(it -> finalFrom == null || syncConfiguration.getCompareId().apply(it, finalFrom) < 0) //
                    .findFirst();
            if (first.isPresent()) {
                from = first.get();
            }

        }

        return from;
    }

    private static <E, I, P> I findTo(SyncConfiguration<I, E, P> syncConfiguration, List<E> sourceEntities, List<P> destinationEntities) {

        I to = null;

        if (!sourceEntities.isEmpty()) {
            to = syncConfiguration.getIdFromEntity().apply(sourceEntities.get(sourceEntities.size() - 1));
        }

        if (!destinationEntities.isEmpty()) {
            I otherId = syncConfiguration.getIdFromPartial().apply(destinationEntities.get(destinationEntities.size() - 1));
            if (to == null) {
                to = otherId;
            } else {
                if (syncConfiguration.getCompareId().apply(otherId, to) < 0) {
                    to = otherId;
                }
            }
        }

        return to;
    }

    /**
     * Execute the sync.
     *
     * @param syncConfiguration
     *            all the methods to sync
     * @param <E>
     *            the entity type
     * @param <I>
     *            the id type
     * @param <P>
     *            the partial entity type
     * @return the added, updated and deleted counts
     */
    public static final <E, I, P> SyncChanges sync(SyncConfiguration<I, E, P> syncConfiguration) {
        return sync(syncConfiguration, new SyncSlice<>());
    }

    /**
     * Execute the sync.
     *
     * @param syncConfiguration
     *            all the methods to sync
     * @param slice
     *            to specify a range of update
     * @param <E>
     *            the entity type
     * @param <I>
     *            the id type
     * @param <P>
     *            the partial entity type
     * @return the added, updated and deleted counts
     */
    public static final <E, I, P> SyncChanges sync(SyncConfiguration<I, E, P> syncConfiguration, SyncSlice<I> slice) {

        // Initial
        SyncChanges changes = new SyncChanges();

        List<E> sourceEntities = null;
        List<P> destinationEntities = null;
        boolean updateSource = true;
        boolean updateDestination = true;

        I fromId = null;
        I toId = null;

        while (updateSource || updateDestination) {

            log.debug("Processing slice: {} ; updateSource {} ; updateDestination {} ", slice, updateSource, updateDestination);

            // Check the destination
            if (updateSource) {
                sourceEntities = syncConfiguration.getSourceSlice().call(slice, syncConfiguration.getMaxSliceSize());
                updateSource = false;
            }
            if (updateDestination) {
                destinationEntities = syncConfiguration.getDestinationSlice().call(slice, syncConfiguration.getMaxSliceSize());
                updateDestination = false;
            }

            // Nothing more
            if (sourceEntities.isEmpty() && destinationEntities.isEmpty()) {
                log.debug("There is nothing else on both sides. Processing completed");

            } else {
                // Find the min and max
                fromId = findFrom(syncConfiguration, slice, sourceEntities, destinationEntities);
                toId = findTo(syncConfiguration, sourceEntities, destinationEntities);
                log.debug("From {} to {}", fromId, toId);
                I finalFromId = fromId;
                I finalToId = toId;

                // Get both iterators
                Iterator<Tuple2<I, E>> sourceIt = sourceEntities.stream() //
                        .map(it -> new Tuple2<>(syncConfiguration.getIdFromEntity().apply(it), it)) //
                        .filter(it -> syncConfiguration.getCompareId().apply(it.getA(), finalFromId) >= 0) //
                        .filter(it -> syncConfiguration.getCompareId().apply(it.getA(), finalToId) <= 0) //
                        .iterator();
                Iterator<Tuple2<I, P>> destinationIt = destinationEntities.stream() //
                        .map(it -> new Tuple2<>(syncConfiguration.getIdFromPartial().apply(it), it)) //
                        .filter(it -> syncConfiguration.getCompareId().apply(it.getA(), finalFromId) >= 0) //
                        .filter(it -> syncConfiguration.getCompareId().apply(it.getA(), finalToId) <= 0) //
                        .iterator();

                // Get first items of both
                Tuple2<I, E> sourceNext = sourceIt.hasNext() ? sourceIt.next() : null;
                Tuple2<I, P> destinationNext = destinationIt.hasNext() ? destinationIt.next() : null;

                List<E> toAdd = new ArrayList<>();
                List<I> toRemove = new ArrayList<>();
                List<E> toUpdate = new ArrayList<>();
                while (sourceNext != null || destinationNext != null) {

                    if (sourceNext == null) {
                        // Destination is extra
                        toRemove.add(destinationNext.getA());
                        destinationNext = destinationIt.hasNext() ? destinationIt.next() : null;
                    } else if (destinationNext == null) {
                        // Source is new
                        toAdd.add(sourceNext.getB());
                        sourceNext = sourceIt.hasNext() ? sourceIt.next() : null;
                    } else {
                        int comparison = syncConfiguration.getCompareId().apply(sourceNext.getA(), destinationNext.getA());
                        if (comparison == 0) {

                            // Check if needs updated
                            if (syncConfiguration.getNeedsUpdate().check(sourceNext.getB(), destinationNext.getB())) {
                                toUpdate.add(sourceNext.getB());
                            }

                            sourceNext = sourceIt.hasNext() ? sourceIt.next() : null;
                            destinationNext = destinationIt.hasNext() ? destinationIt.next() : null;

                        } else if (comparison < 0) {
                            // Missing on destination
                            toAdd.add(sourceNext.getB());
                            sourceNext = sourceIt.hasNext() ? sourceIt.next() : null;

                        } else {
                            // Extra on destination
                            toRemove.add(destinationNext.getA());
                            destinationNext = destinationIt.hasNext() ? destinationIt.next() : null;
                        }
                    }

                }

                // Apply changes
                log.debug("Will apply changes: add {} ; update {} ; delete {}", toAdd.size(), toUpdate.size(), toRemove.size());
                if (!toRemove.isEmpty()) {
                    syncConfiguration.getDeleteHandler().accept(toRemove);
                }
                if (!toUpdate.isEmpty()) {
                    syncConfiguration.getUpdateHandler().accept(toUpdate);
                }
                if (!toAdd.isEmpty()) {
                    syncConfiguration.getAddHandler().accept(toAdd);
                }

                changes.added += toAdd.size();
                changes.updated += toUpdate.size();
                changes.deleted += toRemove.size();

                // Check what needs to advance
                if (!sourceEntities.isEmpty() && //
                        syncConfiguration.getIdFromEntity().apply(sourceEntities.get(sourceEntities.size() - 1)).equals(toId)) {
                    log.debug("Will advance source");
                    updateSource = true;
                }
                if (!destinationEntities.isEmpty() && syncConfiguration.getIdFromPartial().apply(destinationEntities.get(destinationEntities.size() - 1)).equals(toId)) {
                    log.debug("Will advance destination");
                    updateDestination = true;
                }

                slice.setAfterId(Optional.of(toId));
                fromId = toId;

            }
        }

        return changes;

    }
}
