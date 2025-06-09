package com.foilen.smalltools.upgrader.trackers.tasks;

import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.upgrader.trackers.AbstractMongoUpgradeTask;

import java.io.File;

public class V_Export_Schema extends AbstractMongoUpgradeTask {

    private String exportedSchema;

    @Override
    public void execute() {
        try {
            File file = File.createTempFile("exportedSchema", ".txt");
            exportSchema(file.getAbsolutePath());
            exportedSchema = FileTools.getFileAsString(file);
            file.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String getExportedSchema() {
        return exportedSchema;
    }

}
