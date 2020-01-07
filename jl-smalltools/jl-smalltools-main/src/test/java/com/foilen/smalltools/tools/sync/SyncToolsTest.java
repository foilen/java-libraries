/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tuple.Tuple2;

public class SyncToolsTest {

    private List<SyncTestItem> sourceItems = new ArrayList<>();
    private List<SyncTestItem> destinationItems = new ArrayList<>();
    private SyncConfiguration<String, SyncTestItem, Tuple2<String, Long>> syncConfiguration;

    @Before
    public void init() {
        sourceItems = new ArrayList<>();
        destinationItems = new ArrayList<>();

        syncConfiguration = new SyncConfiguration<String, SyncTestItem, Tuple2<String, Long>>() //
                .setMaxSliceSize(7) //
                .setIdFromEntity(it -> it.getId()) //
                .setIdFromPartial(it -> it.getA()) //
                .setCompareId((source, destination) -> source.compareTo(destination)) //
                .setSourceSlice((slice, maxSliceSize) -> sourceItems.stream() //
                        .filter(it -> !slice.getAfterId().isPresent() || it.getId().compareTo(slice.getAfterId().get()) > 0) //
                        .filter(it -> !slice.getBeforeOrEqualId().isPresent() || it.getId().compareTo(slice.getBeforeOrEqualId().get()) <= 0) //
                        .sorted() //
                        .limit(maxSliceSize) //
                        .collect(Collectors.toList()) //
                ) //
                .setDestinationSlice((slice, maxSliceSize) -> destinationItems.stream() //
                        .filter(it -> !slice.getAfterId().isPresent() || it.getId().compareTo(slice.getAfterId().get()) > 0) //
                        .filter(it -> !slice.getBeforeOrEqualId().isPresent() || it.getId().compareTo(slice.getBeforeOrEqualId().get()) <= 0) //
                        .sorted() //
                        .limit(maxSliceSize) //
                        .map(it -> new Tuple2<>(it.getId(), it.getVersion())) //
                        .collect(Collectors.toList()) //
                ) //
                .setNeedsUpdate((source, destination) -> source.getVersion() != destination.getB()) //
                .setAddHandler(entities -> destinationItems.addAll(entities)) //
                .setDeleteHandler(ids -> destinationItems.removeIf(it -> ids.contains(it.getId()))) //
        ;
    }

    @Test
    public void testDestinationAlreadySynced() {
        for (int i = 100; i < 150; ++i) {
            sourceItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
            destinationItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }

        SyncChanges syncChanges = SyncTools.sync(syncConfiguration);
        Assert.assertEquals(0, syncChanges.getAdded());
        Assert.assertEquals(0, syncChanges.getUpdated());
        Assert.assertEquals(0, syncChanges.getDeleted());

        Collections.sort(sourceItems);
        Collections.sort(destinationItems);

        AssertTools.assertJsonComparison(sourceItems, destinationItems);
    }

    @Test
    public void testDestinationEmpty() {

        for (int i = 100; i < 150; ++i) {
            sourceItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }

        SyncChanges syncChanges = SyncTools.sync(syncConfiguration);
        Assert.assertEquals(50, syncChanges.getAdded());
        Assert.assertEquals(0, syncChanges.getUpdated());
        Assert.assertEquals(0, syncChanges.getDeleted());

        Collections.sort(sourceItems);
        Collections.sort(destinationItems);

        AssertTools.assertJsonComparison(sourceItems, destinationItems);

    }

    @Test
    public void testDestinationRandom() {
        for (int i = 110; i < 170; ++i) {
            sourceItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 100; i < 120; ++i) {
            destinationItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 120; i < 200; ++i) {
            destinationItems.add(new SyncTestItem("id" + i, 1, "Content different " + i));
        }

        SyncChanges syncChanges = SyncTools.sync(syncConfiguration);
        Assert.assertEquals(0, syncChanges.getAdded());
        Assert.assertEquals(50, syncChanges.getUpdated());
        Assert.assertEquals(40, syncChanges.getDeleted());

        Collections.sort(sourceItems);
        Collections.sort(destinationItems);

        AssertTools.assertJsonComparison(sourceItems, destinationItems);
    }

    @Test
    public void testDestinationRandom_partialSync() {

        // Initial state
        for (int i = 110; i < 170; ++i) {
            sourceItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 100; i < 120; ++i) {
            destinationItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 120; i < 200; ++i) {
            destinationItems.add(new SyncTestItem("id" + i, 1, "Content different " + i));
        }

        // Expected
        List<SyncTestItem> expectedItems = new ArrayList<>();
        for (int i = 100; i < 106; ++i) {
            expectedItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 110; i <= 130; ++i) {
            expectedItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }
        for (int i = 131; i < 200; ++i) {
            expectedItems.add(new SyncTestItem("id" + i, 1, "Content different " + i));
        }

        // Execute and assert
        SyncChanges syncChanges = SyncTools.sync(syncConfiguration, new SyncSlice<String>().setAfterId(Optional.of("id105")).setBeforeOrEqualId(Optional.of("id130")));
        Assert.assertEquals(0, syncChanges.getAdded());
        Assert.assertEquals(11, syncChanges.getUpdated());
        Assert.assertEquals(4, syncChanges.getDeleted());

        Collections.sort(expectedItems);
        Collections.sort(destinationItems);

        AssertTools.assertJsonComparison(expectedItems, destinationItems);
    }

    @Test
    public void testSourceEmpty() {

        for (int i = 100; i < 150; ++i) {
            destinationItems.add(new SyncTestItem("id" + i, 0, "Content " + i));
        }

        SyncChanges syncChanges = SyncTools.sync(syncConfiguration);
        Assert.assertEquals(0, syncChanges.getAdded());
        Assert.assertEquals(0, syncChanges.getUpdated());
        Assert.assertEquals(50, syncChanges.getDeleted());

        Collections.sort(sourceItems);
        Collections.sort(destinationItems);

        AssertTools.assertJsonComparison(sourceItems, destinationItems);

    }

}
