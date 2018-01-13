/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

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
    public void testDefaultDiscard() throws Exception {
        // Prepare tmp files
        File renameSrc = File.createTempFile("junit", null);
        File renameDst = File.createTempFile("junit", null);
        renameSrc.delete();
        renameDst.delete();

        // Create the wrapper
        FileOutputStream wrappedOutputStream = new FileOutputStream(renameSrc);
        RenamingOnCloseOutputStreamWrapper renamingOnCloseOutputStreamWrapper = new RenamingOnCloseOutputStreamWrapper(wrappedOutputStream, renameSrc, renameDst, true);
        Assert.assertTrue(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

        // Send some data to it and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the discarding
        Assert.assertFalse(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

    }

    @Test
    public void testDefaultDiscard_ChangeToRename() throws Exception {
        // Prepare tmp files
        File renameSrc = File.createTempFile("junit", null);
        File renameDst = File.createTempFile("junit", null);
        renameSrc.delete();
        renameDst.delete();

        // Create the wrapper
        FileOutputStream wrappedOutputStream = new FileOutputStream(renameSrc);
        RenamingOnCloseOutputStreamWrapper renamingOnCloseOutputStreamWrapper = new RenamingOnCloseOutputStreamWrapper(wrappedOutputStream, renameSrc, renameDst, true);
        Assert.assertTrue(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

        // Send some data to it, change state and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.setDeleteOnClose(false);
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the renaming
        Assert.assertFalse(renameSrc.exists());
        Assert.assertTrue(renameDst.exists());

        // Assert the final content
        Assert.assertEquals("Hello World", FileTools.getFileAsString(renameDst));
    }

    @Test
    public void testDefaultDiscard_DoubleClose() throws Exception {
        // Prepare tmp files
        File renameSrc = File.createTempFile("junit", null);
        File renameDst = File.createTempFile("junit", null);
        renameSrc.delete();
        renameDst.delete();

        // Create the wrapper
        FileOutputStream wrappedOutputStream = new FileOutputStream(renameSrc);
        RenamingOnCloseOutputStreamWrapper renamingOnCloseOutputStreamWrapper = new RenamingOnCloseOutputStreamWrapper(wrappedOutputStream, renameSrc, renameDst, true);
        Assert.assertTrue(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

        // Send some data to it and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.close();
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the discarding
        Assert.assertFalse(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

    }

    @Test
    public void testDefaultRenaming() throws Exception {
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
    public void testDefaultRenaming_ChangeToDiscard() throws Exception {
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

        // Send some data to it, change state and close
        renamingOnCloseOutputStreamWrapper.write("Hello World".getBytes(CharsetTools.UTF_8));
        renamingOnCloseOutputStreamWrapper.setDeleteOnClose(true);
        renamingOnCloseOutputStreamWrapper.close();

        // Assert the discarding
        Assert.assertFalse(renameSrc.exists());
        Assert.assertFalse(renameDst.exists());

    }

    @Test
    public void testDefaultRenaming_DoubleClose() throws Exception {
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
