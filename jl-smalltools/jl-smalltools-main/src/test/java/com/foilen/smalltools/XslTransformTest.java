/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.ResourceTools;

/**
 * Tests for {@link XslTransform}.
 */
public class XslTransformTest {

    private static String INPUT_RESOURCE_PATH = "/com/foilen/smalltools/XslTransformTest-resources/input.xml";
    private static String INPUT2_RESOURCE_PATH = "/com/foilen/smalltools/XslTransformTest-resources/input2.xml";
    private static String TRANSFORMATION_RESOURCE_PATH = "/com/foilen/smalltools/XslTransformTest-resources/transformation.xsl";
    private static String OUTPUT_RESOURCE_PATH = "/com/foilen/smalltools/XslTransformTest-resources/output.xml";
    private static String OUTPUT2_RESOURCE_PATH = "/com/foilen/smalltools/XslTransformTest-resources/output2.xml";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMultipleCalls() throws IOException {

        String xsl = ResourceTools.getResourceAsString(TRANSFORMATION_RESOURCE_PATH);

        // First call
        String xml = ResourceTools.getResourceAsString(INPUT_RESOURCE_PATH);
        String expected = ResourceTools.getResourceAsString(OUTPUT_RESOURCE_PATH);
        String actual = new XslTransform().usingText(xsl).fromText(xml).toText();
        Assert.assertEquals(expected, actual);

        // Second call
        xml = ResourceTools.getResourceAsString(INPUT2_RESOURCE_PATH);
        expected = ResourceTools.getResourceAsString(OUTPUT2_RESOURCE_PATH);
        actual = new XslTransform().usingText(xsl).fromText(xml).toText();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUsingFileNames() throws IOException {

        File xslFile = File.createTempFile("junit", null);
        File xmlFile = File.createTempFile("junit", null);
        File finalFile = File.createTempFile("junit", null);

        ResourceTools.copyToFile(TRANSFORMATION_RESOURCE_PATH, xslFile);
        ResourceTools.copyToFile(INPUT_RESOURCE_PATH, xmlFile);

        new XslTransform().usingFile(xslFile.getAbsolutePath()).fromFile(xmlFile.getAbsolutePath()).toFile(finalFile.getAbsolutePath());

        String expected = ResourceTools.getResourceAsString(OUTPUT_RESOURCE_PATH);
        String actual = FileTools.getFileAsString(finalFile.getAbsolutePath());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUsingFiles() throws IOException {

        File xslFile = File.createTempFile("junit", null);
        File xmlFile = File.createTempFile("junit", null);
        File finalFile = File.createTempFile("junit", null);

        ResourceTools.copyToFile(TRANSFORMATION_RESOURCE_PATH, xslFile);
        ResourceTools.copyToFile(INPUT_RESOURCE_PATH, xmlFile);

        new XslTransform().usingFile(xslFile).fromFile(xmlFile).toFile(finalFile);

        String expected = ResourceTools.getResourceAsString(OUTPUT_RESOURCE_PATH);
        String actual = FileTools.getFileAsString(finalFile);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUsingResources() {
        String actual = new XslTransform().usingResource(TRANSFORMATION_RESOURCE_PATH).fromResource(INPUT_RESOURCE_PATH).toText();
        String expected = ResourceTools.getResourceAsString(OUTPUT_RESOURCE_PATH);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUsingText() throws IOException {

        String xsl = ResourceTools.getResourceAsString(TRANSFORMATION_RESOURCE_PATH);
        String xml = ResourceTools.getResourceAsString(INPUT_RESOURCE_PATH);

        String expected = ResourceTools.getResourceAsString(OUTPUT_RESOURCE_PATH);
        String actual = new XslTransform().usingText(xsl).fromText(xml).toText();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testXmlNotSet() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("XML not set. Call any from* methods prior");

        new XslTransform().usingResource(TRANSFORMATION_RESOURCE_PATH).toText();
    }

    @Test
    public void testXslNotSet() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("XSL not set. Call any using* methods prior");

        new XslTransform().fromResource(INPUT_RESOURCE_PATH).toText();
    }

}
