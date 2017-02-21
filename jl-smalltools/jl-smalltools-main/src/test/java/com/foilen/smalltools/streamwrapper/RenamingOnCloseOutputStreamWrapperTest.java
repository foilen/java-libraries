/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.FileTools;

/**
 * Tests for {@link RenamingOnCloseOutputStreamWrapper}.
 */
public class RenamingOnCloseOutputStreamWrapperTest {

    @Test
    public void test() throws Exception {
        // Prepare tmp files
        File renameSrc = File.createTempFile("junit", null);
        File renameDst = File.createTempFile("junit", null);
        renameSrc.delete();
        renameDst.delete();

        // Create the wrapper
        FileOutputStream wrappedOutputStream = new FileOutputStream(renameSrc);
        RenamingOnCloseOutputStreamWrapper renamingOnCloseOutputStreamWrapper = new RenamingOnCloseOutputStreamWrapper(wrappedOutputStream, renameSrc, renameDst);
        Assert.assertTrue(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

        // Send some data to it and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the renaming
        Assert.assertFalse(renameSrc.exists());
        Assert.assertTrue(renameDst.exists());

        // Assert the final content
        Assert.assertEquals("Hello World", FileTools.getFileAsString(renameDst));

    }

    @Test
    public void testDoubleClose() throws Exception {
        // Prepare tmp files
        File renameSrc = File.createTempFile("junit", null);
        File renameDst = File.createTempFile("junit", null);
        renameSrc.delete();
        renameDst.delete();

        // Create the wrapper
        FileOutputStream wrappedOutputStream = new FileOutputStream(renameSrc);
        RenamingOnCloseOutputStreamWrapper renamingOnCloseOutputStreamWrapper = new RenamingOnCloseOutputStreamWrapper(wrappedOutputStream, renameSrc, renameDst);
        Assert.assertTrue(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

        // Send some data to it and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.close();
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the renaming
        Assert.assertFalse(renameSrc.exists());
        Assert.assertTrue(renameDst.exists());

        // Assert the final content
        Assert.assertEquals("Hello World", FileTools.getFileAsString(renameDst));

    }

}
