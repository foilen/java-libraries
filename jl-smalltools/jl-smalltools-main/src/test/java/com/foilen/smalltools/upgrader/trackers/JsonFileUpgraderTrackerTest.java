/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
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
