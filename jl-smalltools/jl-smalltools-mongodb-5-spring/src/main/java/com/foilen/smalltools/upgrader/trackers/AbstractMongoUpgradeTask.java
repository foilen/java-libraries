package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.*;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tasks with helpers to manage a mongodb database.
 */
public abstract class AbstractMongoUpgradeTask extends AbstractBasics implements UpgradeTask {

    @Autowired
    protected MongoClient mongoClient;
    @Autowired
    protected MongoOperations mongoOperations;

    @Value("${spring.data.mongodb.database}")
    protected String databaseName;

    /**
     * Add a collection if it does not exist.
     *
     * @param collectionName the name of the collection
     */
    protected void addCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create collection {}", collectionName);
        try {
            mongoDatabase.createCollection(collectionName);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    /**
     * Add a collection if it does not exist.
     *
     * @param collectionName          the name of the collection
     * @param createCollectionOptions the options for the collection
     */
    protected void addCollection(String collectionName, CreateCollectionOptions createCollectionOptions) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create collection {} with options {}", collectionName, createCollectionOptions);
        try {
            mongoDatabase.createCollection(collectionName, createCollectionOptions);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    /**
     * Add an index if it does not exist.
     *
     * @param collectionName the name of the collection
     * @param indexOptions   the options for the index
     * @param keys           the keys to index
     */
    @SafeVarargs
    protected final void addIndex(String collectionName, IndexOptions indexOptions, Tuple2<String, Object>... keys) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create index for collection {} , with keys {}", collectionName, keys);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document keysDocument = new Document();
        for (Tuple2<String, Object> key : keys) {
            keysDocument.put(key.getA(), key.getB());
        }
        collection.createIndex(keysDocument, indexOptions);
    }

    /**
     * Add an index if it does not exist.
     *
     * @param collectionName the name of the collection
     * @param keys           the keys to index
     */
    @SafeVarargs
    protected final void addIndex(String collectionName, Tuple2<String, Object>... keys) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create index for collection {} , with keys {}", collectionName, keys);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document keysDocument = new Document();
        for (Tuple2<String, Object> key : keys) {
            keysDocument.put(key.getA(), key.getB());
        }
        collection.createIndex(keysDocument);
    }

    /**
     * Add a view if it does not exist.
     *
     * @param viewName the name of the view
     * @param viewOn   the collection to view
     * @param pipeline the pipeline to apply
     */
    protected void addView(String viewName, String viewOn, List<? extends Bson> pipeline) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create view {}", viewName);
        try {
            mongoDatabase.createView(viewName, viewOn, pipeline);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    protected void insertInCollection(String collectionName, String resourceName, Class<?> resourceClass) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Insert in collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        BufferBatchesTools.<Document>autoClose(100, collection::insertMany, documents -> {
            ResourceTools.readResourceLinesIteration(resourceName, resourceClass).forEach(line -> {
                logger.info("Batching line {}", line);
                Document document = Document.parse(line);
                documents.add(document);
            });
        });

    }

    protected void insertOrUpdateInCollection(String collectionName, String resourceName, Class<?> resourceClass) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Insert or update in collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        ResourceTools.readResourceLinesIteration(resourceName, resourceClass).forEach(line -> {
            logger.info("Line {}", line);
            Document document = Document.parse(line);

            // Check if exists
            if (collection.find(Filters.eq("_id", document.get("_id"))).first() == null) {
                logger.info("Inserting {}", line);
                collection.insertOne(document);
            } else {
                logger.info("Updating {}", line);
                collection.replaceOne(Filters.eq("_id", document.get("_id")), document);
            }

        });

    }

    protected void exportFromCollection(String fileName, String collectionName, Bson filter, Consumer<FindIterable<Document>> findConsumer) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Export from collection {} to file {}", collectionName, fileName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        var findIterator = collection.find(filter);
        findConsumer.accept(findIterator);

        try (PrintWriter out = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
            findIterator.forEach(document -> {
                String line = document.toJson();
                logger.info("Exporting {}", line);
                out.println(line);
            });
        } catch (Exception e) {
            throw new RuntimeException("Problem exporting", e);
        }

    }

    protected void exportSchema(String fileName) {
        try (PrintWriter out = new PrintWriter(fileName, StandardCharsets.UTF_8)) {

            MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
            StreamTools.toStream(mongoDatabase.listCollections().spliterator())
                    .sorted(Comparator.comparing(a -> a.get("name").toString()))
                    .forEach(collection -> {
                        logger.info("Exporting schema for {}", collection);

                        String name = collection.getString("name");
                        if (name.startsWith("system.") || StringTools.safeEquals(name, "upgraderTools")) {
                            logger.info("Skipping {}", name);
                            return;
                        }
                        String type = collection.getString("type");
                        var options = collection.get("options", Document.class);
                        switch (type) {
                            case "collection":
                                if (options.isEmpty()) {
                                    out.println("addCollection(\"" + name + "\");");
                                } else {
                                    List<String> collectionOptions = new ArrayList<>();
                                    if (options.getBoolean("capped")) {
                                        collectionOptions.add(".capped(true)");
                                    }
                                    if (options.containsKey("max")) {
                                        collectionOptions.add(".maxDocuments(" + options.get("max") + ")");
                                    }
                                    if (options.containsKey("size")) {
                                        collectionOptions.add(".sizeInBytes(" + options.get("size") + ")");
                                    }
                                    out.println("addCollection(\"" + name + "\", new CreateCollectionOptions()" + String.join("", collectionOptions) + ");");
                                }

                                // Indexes
                                var indexes = mongoDatabase.getCollection(name).listIndexes();
                                for (Document index : indexes) {
                                    logger.info("Collection {} has index {}", name, index);
                                    if (StringTools.safeEquals("_id_", index.getString("name"))) {
                                        logger.info("Skipping index {}", index);
                                        continue;
                                    }
                                    var keys = index.get("key", Document.class);
                                    List<String> indexOptionsList = new ArrayList<>();
                                    if (index.containsKey("2dsphereIndexVersion")) {
                                        indexOptionsList.add(".sphereVersion(" + index.get("2dsphereIndexVersion") + ")");
                                    }
                                    if (index.containsKey("background")) {
                                        indexOptionsList.add(".background(true)");
                                    }
                                    if (index.containsKey("bits")) {
                                        indexOptionsList.add(".bits(" + index.get("bits") + ")");
                                    }
                                    if (index.containsKey("bucketSize")) {
                                        indexOptionsList.add(".bucketSize(" + index.get("bucketSize") + ")");
                                    }
                                    if (index.containsKey("default_language")) {
                                        indexOptionsList.add(".defaultLanguage(\"" + index.get("default_language") + "\")");
                                    }
                                    if (index.containsKey("dropDups")) {
                                        indexOptionsList.add(".dropDups(true)");
                                    }
                                    if (index.containsKey("name")) {
                                        indexOptionsList.add(".name(\"" + index.get("name") + "\")");
                                    }
                                    if (index.containsKey("expireAfterSeconds")) {
                                        indexOptionsList.add(".expireAfter(" + index.get("expireAfterSeconds") + ", TimeUnit.SECONDS)");
                                    }
                                    if (index.containsKey("collation")) {
                                        indexOptionsList.add(".collation(" + outputNewDocument(index.get("collation", Document.class)) + ")");
                                    }
                                    if (index.containsKey("language_override")) {
                                        indexOptionsList.add(".languageOverride(\"" + index.get("language_override") + "\")");
                                    }
                                    if (index.containsKey("min")) {
                                        indexOptionsList.add(".min(" + index.get("min") + ")");
                                    }
                                    if (index.containsKey("max")) {
                                        indexOptionsList.add(".max(" + index.get("max") + ")");
                                    }
                                    if (index.containsKey("partialFilterExpression")) {
                                        indexOptionsList.add(".partialFilterExpression(" + outputNewDocument(index.get("partialFilterExpression", Document.class)) + ")");
                                    }
                                    if (index.containsKey("sparse")) {
                                        indexOptionsList.add(".sparse(true)");
                                    }
                                    if (index.containsKey("storageEngine")) {
                                        indexOptionsList.add(".storageEngine(" + outputNewDocument(index.get("storageEngine", Document.class)) + ")");
                                    }
                                    if (index.containsKey("textIndexVersion")) {
                                        indexOptionsList.add(".textVersion(" + index.get("textIndexVersion") + ")");
                                    }
                                    if (index.containsKey("unique")) {
                                        indexOptionsList.add(".unique(true)");
                                    }
                                    if (index.containsKey("weights")) {
                                        indexOptionsList.add(".weights(" + outputNewDocument(index.get("weights", Document.class)) + ")");
                                    }
                                    List<String> keyTuples = new ArrayList<>();
                                    keys.forEach((key, value) -> {
                                        keyTuples.add("  new Tuple2<>(\"" + key + "\", " + value + ")");
                                    });
                                    out.println("addIndex(\"" + name + "\",\n  new IndexOptions()" +
                                            String.join("", indexOptionsList) + ",\n" +
                                            String.join(",\n", keyTuples) + "\n);");
                                }
                                break;
                            case "view":
                                String viewOn = options.getString("viewOn");
                                out.println("addView(\"" + name + "\", \"" + viewOn + "\",");
                                out.println("  List.of(");
                                List<Document> pipeline = options.getList("pipeline", Document.class);
                                for (int i = 0; i < pipeline.size(); i++) {
                                    Document document = pipeline.get(i);
                                    out.println("    " + outputNewDocument(document) + (i == pipeline.size() - 1 ? "" : ","));
                                }
                                out.println("  )");
                                out.println(");");
                                break;
                        }

                        out.println();
                    });

        } catch (Exception e) {
            throw new RuntimeException("Problem exporting", e);
        }
    }

    private String outputNewDocument(Document document) {
        StringBuilder sb = new StringBuilder();
        List<String> parts = new ArrayList<>();
        document.forEach((key, value) -> {
            if (value instanceof String) {
                parts.add("\"" + key + "\", \"" + value + "\"");
            } else if (value instanceof Document subDocument) {
                parts.add("\"" + key + "\", " + outputNewDocument(subDocument));
            } else {
                parts.add("\"" + key + "\", " + value);
            }
        });
        sb.append("new Document(");
        sb.append(parts.get(0));
        if (parts.size() > 1) {
            logger.warn("More than one part is not supported for {}", document);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Drop a collection if it exists.
     *
     * @param collectionName the name of the collection
     */
    protected void dropCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Drop collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();
    }

    /**
     * Get the database name.
     *
     * @return the database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the mongo client.
     *
     * @return the mongo client
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Set the database name.
     *
     * @param databaseName the database name
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Set the mongo client.
     *
     * @param mongoClient the mongo client
     */
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public String useTracker() {
        return "mongodb";
    }

}
