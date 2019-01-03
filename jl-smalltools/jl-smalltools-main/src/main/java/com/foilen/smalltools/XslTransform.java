/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CharsetTools;

/**
 * To transform an XML using XSL.
 *
 * <pre>
 * - Create an instance
 * - call a using* to set the XSL
 * - call a from* to set the source XML
 * - call a to* to make the transformation and store the XML
 * </pre>
 */
public class XslTransform {

    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();

    private Transformer transformer;
    private StreamSource fromXml;

    public XslTransform() {
    }

    /**
     * Check all fields have a value.
     */
    private void assertFields() {
        AssertTools.assertNotNull(transformer, "XSL not set. Call any using* methods prior");
        AssertTools.assertNotNull(fromXml, "XML not set. Call any from* methods prior");
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlFile
     *            the XML file
     * @return this
     */
    public XslTransform fromFile(File xmlFile) {
        try {
            fromXml = new StreamSource(xmlFile);
            return this;
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlPathName
     *            the XML file path
     * @return this
     */
    public XslTransform fromFile(String xmlPathName) {
        try {
            return fromInputStream(new FileInputStream(xmlPathName));
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlInputStream
     *            the XML as a stream
     * @return this
     */
    public XslTransform fromInputStream(InputStream xmlInputStream) {
        fromXml = new StreamSource(xmlInputStream);
        return this;
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlResourcePath
     *            the XML absolute resource path
     * @return this
     */
    public XslTransform fromResource(String xmlResourcePath) {
        return fromInputStream(getClass().getResourceAsStream(xmlResourcePath));
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlContent
     *            the XML content as a String
     * @return this
     */
    public XslTransform fromText(String xmlContent) {
        return fromInputStream(new ByteArrayInputStream(xmlContent.getBytes(CharsetTools.UTF_8)));
    }

    /**
     * Set the XML to transform.
     *
     * @param xmlUrl
     *            the URL to the XML file
     * @return this
     */
    public XslTransform fromUrl(String xmlUrl) {
        try {
            fromXml = new StreamSource(xmlUrl);
            return this;
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Transform and store.
     *
     * @param xmlFile
     *            the destination file
     */
    public void toFile(File xmlFile) {
        assertFields();
        try {
            StreamResult out = new StreamResult(xmlFile);
            transformer.transform(fromXml, out);
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Transform and store.
     *
     * @param xmlPathName
     *            the destination file path
     */
    public void toFile(String xmlPathName) {
        assertFields();
        try {
            toOutputStream(new FileOutputStream(xmlPathName));
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Transform and store.
     *
     * @param xmlOutputStream
     *            the destination as a stream
     */
    public void toOutputStream(OutputStream xmlOutputStream) {
        assertFields();
        try {
            StreamResult out = new StreamResult(xmlOutputStream);
            transformer.transform(fromXml, out);
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Transform and store.
     *
     * @return the XML as String
     */
    public String toText() {
        assertFields();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            toOutputStream(outputStream);
            return new String(outputStream.toByteArray());
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Transform and store.
     *
     * @param xmlWriter
     *            the destination as a writer
     */
    public void toWriter(Writer xmlWriter) {
        assertFields();
        try {
            StreamResult out = new StreamResult(xmlWriter);
            transformer.transform(fromXml, out);
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XSL to use.
     *
     * @param xslFile
     *            the XSL file
     * @return this
     */
    public XslTransform usingFile(File xslFile) {
        try {
            transformer = FACTORY.newTransformer(new StreamSource(xslFile));
            return this;
        } catch (TransformerConfigurationException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XSL to use.
     *
     * @param xslPathName
     *            the XSL file path
     * @return this
     */
    public XslTransform usingFile(String xslPathName) {
        try {
            transformer = FACTORY.newTransformer(new StreamSource(new File(xslPathName)));
            return this;
        } catch (TransformerConfigurationException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XSL to use.
     *
     * @param xslInputStream
     *            the xsl input stream
     * @return this
     */
    public XslTransform usingInputStream(InputStream xslInputStream) {
        try {
            transformer = FACTORY.newTransformer(new StreamSource(xslInputStream));
            return this;
        } catch (TransformerConfigurationException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XSL to use.
     *
     * @param xslReader
     *            the xsl reader
     * @return this
     */
    public XslTransform usingReader(Reader xslReader) {
        try {
            transformer = FACTORY.newTransformer(new StreamSource(xslReader));
            return this;
        } catch (TransformerConfigurationException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Set the XSL to use.
     *
     * @param xslResourcePath
     *            the XSL absolute resource path
     * @return this
     */
    public XslTransform usingResource(String xslResourcePath) {
        return usingInputStream(getClass().getResourceAsStream(xslResourcePath));
    }

    /**
     * Set the XSL to use.
     *
     * @param xslContent
     *            the XSL full content as a String
     * @return this
     */
    public XslTransform usingText(String xslContent) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xslContent.getBytes(CharsetTools.UTF_8));
        return usingInputStream(inputStream);
    }

    /**
     * Set the XSL to use.
     *
     * @param xslUrl
     *            the URL to the XSL file
     * @return this
     */
    public XslTransform usingUrl(String xslUrl) {
        try {
            transformer = FACTORY.newTransformer(new StreamSource(xslUrl));
            return this;
        } catch (TransformerConfigurationException e) {
            throw new SmallToolsException(e);
        }
    }

}
