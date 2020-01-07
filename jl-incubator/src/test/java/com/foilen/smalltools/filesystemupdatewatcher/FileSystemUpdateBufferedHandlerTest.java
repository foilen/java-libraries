/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.tuple.Tuple2;

public class FileSystemUpdateBufferedHandlerTest {

    static private class FileSystemUpdateMockHandler implements FileSystemUpdateHandler {

        private List<Tuple2<String, File>> events = new ArrayList<>();

        public void assertEvent(String type, File file) {
            boolean event = events.remove(new Tuple2<>(type, file));
            Assert.assertTrue(type + " " + file.getPath() + " not received", event);
        }

        public void assertNoMore() {
            if (!events.isEmpty()) {
                Assert.fail("Still got events: " + events);
            }
        }

        @Override
        public void created(File file) {
            events.add(new Tuple2<>("created", file));
        }

        @Override
        public void deleted(File file) {
            events.add(new Tuple2<>("deleted", file));
        }

        @Override
        public void modified(File file) {
            events.add(new Tuple2<>("modified", file));
        }
    }

    @Test(timeout = 10000)
    public void test() {
        FileSystemUpdateMockHandler mockHandler = new FileSystemUpdateMockHandler();
        FileSystemUpdateBufferedHandler bufferedHandler = new FileSystemUpdateBufferedHandler(mockHandler, 1000, 7000);

        // 1 Created + X Modified = 1 Created
        File file = new File("newAndUpdated_1");
        bufferedHandler.created(file);
        bufferedHandler.modified(file);
        bufferedHandler.modified(file);

        file = new File("newAndUpdated_2");
        bufferedHandler.created(file);
        bufferedHandler.modified(file);

        file = new File("new_3");
        bufferedHandler.created(file);

        // 1 Created + X Modified + 1 Deleted = 0 event
        file = new File("neverCreated_noEvent_1");
        bufferedHandler.created(file);
        bufferedHandler.modified(file);
        bufferedHandler.deleted(file);

        file = new File("neverCreated_noEvent_2");
        bufferedHandler.created(file);
        bufferedHandler.deleted(file);

        file = new File("neverCreated_noEvent_3");
        bufferedHandler.created(file);
        bufferedHandler.deleted(file);
        bufferedHandler.created(file);
        bufferedHandler.modified(file);
        bufferedHandler.deleted(file);

        // X Modified + 1 Deleted = 1 Deleted
        file = new File("deleted_1");
        bufferedHandler.modified(file);
        bufferedHandler.modified(file);
        bufferedHandler.deleted(file);

        file = new File("deleted_2");
        bufferedHandler.deleted(file);

        // 1 Deleted + 1 Created = 1 Modified
        file = new File("modified_1");
        bufferedHandler.deleted(file);
        bufferedHandler.created(file);

        file = new File("modified_2");
        bufferedHandler.modified(file);
        bufferedHandler.deleted(file);
        bufferedHandler.created(file);

        file = new File("modified_3");
        bufferedHandler.deleted(file);
        bufferedHandler.created(file);
        bufferedHandler.modified(file);

        // Wait
        ThreadTools.sleep(4000);

        // Validate
        mockHandler.assertEvent("created", new File("new_3"));
        mockHandler.assertEvent("created", new File("newAndUpdated_1"));
        mockHandler.assertEvent("created", new File("newAndUpdated_2"));

        mockHandler.assertEvent("deleted", new File("deleted_1"));
        mockHandler.assertEvent("deleted", new File("deleted_2"));

        mockHandler.assertEvent("modified", new File("modified_1"));
        mockHandler.assertEvent("modified", new File("modified_2"));
        mockHandler.assertEvent("modified", new File("modified_3"));

        mockHandler.assertNoMore();
    }

}
