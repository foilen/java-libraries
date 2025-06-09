package com.foilen.smalltools.upgrader.trackers;

import java.io.File;
import java.io.IOException;

public class JsonFileUpgraderTrackerTest extends AbstractUpgraderTrackerTest {

    public JsonFileUpgraderTrackerTest() throws IOException {
        File file = File.createTempFile("upgrader", null);
        file.delete();
        init(new JsonFileUpgraderTracker(file.getAbsolutePath()));
    }

}
