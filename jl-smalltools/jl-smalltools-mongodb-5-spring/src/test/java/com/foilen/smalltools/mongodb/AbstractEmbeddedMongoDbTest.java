/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb;

import com.foilen.smalltools.tools.AbstractBasics;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractEmbeddedMongoDbTest extends AbstractBasics {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractEmbeddedMongoDbTest.class);

    protected static MongoClient mongoClient;
    private static TransitionWalker.ReachedState<RunningMongodProcess> running;

    @BeforeAll
    public static void beforeAll() {
        var running = new Mongod() {
            @Override
            public Transition<MongodArguments> mongodArguments() {
                return Start.to(MongodArguments.class)
                        .initializedWith(MongodArguments.defaults()
                                .withReplication(Storage.of("rs", 5000))
                        );
            }
        }.transitions(Version.Main.V7_0)
                .walker()
                .initState(StateID.of(RunningMongodProcess.class));
        Runtime.getRuntime().addShutdownHook(new Thread(running::close));

        String uriForAdmin = "mongodb://" + running.current().getServerAddress().toString();
        logger.info("uriForAdmin: {}", uriForAdmin);
        MongoClient adminMongoClient = MongoClients.create(uriForAdmin);
        adminMongoClient.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));

        String uriWithRs = "mongodb://" + running.current().getServerAddress().toString() + "/?replicaSet=rs";
        logger.info("uriWithRs: {}", uriWithRs);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uriWithRs))
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(10000)
                )
                .build();

        mongoClient = MongoClients.create(settings);
    }

    @AfterAll
    public static void stopMongoDB() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
        if (running != null) {
            running.close();
            running = null;
        }
    }

}
