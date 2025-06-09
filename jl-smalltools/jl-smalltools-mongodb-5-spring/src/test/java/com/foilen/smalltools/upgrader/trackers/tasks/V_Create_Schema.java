package com.foilen.smalltools.upgrader.trackers.tasks;

import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.trackers.AbstractMongoUpgradeTask;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class V_Create_Schema extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {

        addCollection("col1");
        addIndex("col1",
                new Tuple2<>("field1", 1));
        addIndex("col1",
                new Tuple2<>("field1", 1),
                new Tuple2<>("field2", 1)
        );

        addCollection("col2");
        addIndex("col2",
                new IndexOptions().unique(true),
                new Tuple2<>("u1", 1),
                new Tuple2<>("u2", 1)
        );
        addIndex("col2",
                new IndexOptions().sparse(true),
                new Tuple2<>("s1", 1)
        );
        addIndex("col2",
                new IndexOptions().expireAfter(10L, TimeUnit.HOURS),
                new Tuple2<>("ttlTime", 1)
        );

        addCollection("col3");
        addIndex("col3",
                new Tuple2<>("comment", "text")
        );

        addCollection("col4", new CreateCollectionOptions().capped(true).maxDocuments(100).sizeInBytes(1000));

        addView("view1", "col1",
                List.of(
                        new Document("$match", new Document("status", "A")),
                        new Document("$group", new Document("_id", "$cust_id"))
                )
        );

    }
}
