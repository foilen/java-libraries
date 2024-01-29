/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.AbstractBasics;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
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
        running = Mongod.instance().start(Version.Main.V7_0);
        String uri = "mongodb://" + running.current().getServerAddress().toString();
        logger.info("uri: {}", uri);
        mongoClient = MongoClients.create(uri);
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
