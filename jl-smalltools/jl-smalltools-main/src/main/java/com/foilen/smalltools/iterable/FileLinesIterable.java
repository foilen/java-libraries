/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.iterable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.FileTools;

/**
 * This is an iterable over a file. Each line will be iterated over. The file is automatically closed at the end of the file. Used by {@link FileTools#readFileLinesIteration(String)}
 */
public class FileLinesIterable implements Iterable<String>, Iterator<String> {

    private BufferedReader bufferedReader;
    private String nextLine;

    private void close() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            // This is just a close, so we can safely ignore
        }
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public String next() {
        String result = nextLine;
        readNextLine();
        return result;
    }

    /**
     * The file to open.
     *
     * @param file
     *            the file
     * @throws FileNotFoundException
     *             FileNotFoundException
     */
    public void openFile(File file) throws FileNotFoundException {
        bufferedReader = new BufferedReader(new FileReader(file));
        readNextLine();
    }

    /**
     * The stream to use.
     *
     * @param inputStream
     *            the stream to read
     */
    public void openStream(InputStream inputStream) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, CharsetTools.UTF_8));
        readNextLine();
    }

    /**
     * The String to use.
     *
     * @param text
     *            the text to read
     */
    public void openString(String text) {
        openStream(new ByteArrayInputStream(text.getBytes(CharsetTools.UTF_8)));
    }

    /**
     * Read and store the next line in a field. When EOF or error reading, closing the resource.
     */
    private void readNextLine() {
        try {
            nextLine = bufferedReader.readLine();
            if (nextLine == null) {
                close();
            }
        } catch (IOException e) {
            close();
            throw new SmallToolsException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}