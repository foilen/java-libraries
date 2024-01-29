/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filenamefilter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * To retrieve only the directories.
 */
public class DirectoriesFileFilter implements FilenameFilter {

    /**
     * Accept only directories.
     *
     * @param dir  the directory in which the file was found.
     * @param name the name of the file.
     * @return true if is a directory
     */
    @Override
    public boolean accept(File dir, String name) {
        return (new File(dir.getAbsoluteFile() + File.separator + name).isDirectory());
    }

}
