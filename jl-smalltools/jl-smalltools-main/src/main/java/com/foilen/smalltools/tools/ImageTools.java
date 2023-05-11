/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageTools {

    private static final Logger logger = LoggerFactory.getLogger(ImageTools.class);

    /**
     * Scale the image.
     *
     * @param inputFile        the input file
     * @param scale            the scale (1.0 = 100%)
     * @param outputFormatName the output format name (jpg, png, ...)
     * @return a temporary file with the scaled image that you can delete after use
     */
    public static File scale(File inputFile, float scale, String outputFormatName) {
        try {
            BufferedImage inputImage = ImageIO.read(inputFile);

            // Calculate the scaled image dimensions
            int scaledWitdh = (int) (inputImage.getWidth() * scale);
            int scaledHeight = (int) (inputImage.getHeight() * scale);

            // Scale the image
            Image scaledImage = inputImage.getScaledInstance(scaledWitdh, scaledHeight, Image.SCALE_SMOOTH);

            // Create the output image
            BufferedImage outputImage = new BufferedImage(scaledWitdh, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = outputImage.createGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();

            // Write the image to the output file
            File outputFile = File.createTempFile("scaledImage", "." + outputFormatName);
            ImageIO.write(outputImage, outputFormatName, outputFile);

            return outputFile;
        } catch (Exception e) {
            logger.error("Problem scaling", e);
            return null;
        }
    }

    /**
     * Scale the image.
     *
     * @param inputStream      the input stream
     * @param scale            the scale (1.0 = 100%)
     * @param outputFormatName the output format name (jpg, png, ...)
     * @param outputStream     the output stream to write to
     */
    public static void scale(InputStream inputStream, float scale, String outputFormatName, OutputStream outputStream) {
        try {
            BufferedImage inputImage = ImageIO.read(inputStream);

            // Calculate the scaled image dimensions
            int scaledWitdh = (int) (inputImage.getWidth() * scale);
            int scaledHeight = (int) (inputImage.getHeight() * scale);

            // Scale the image
            Image scaledImage = inputImage.getScaledInstance(scaledWitdh, scaledHeight, Image.SCALE_SMOOTH);

            // Create the output image
            BufferedImage outputImage = new BufferedImage(scaledWitdh, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = outputImage.createGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();

            // Write the image to the output stream
            ImageIO.write(outputImage, outputFormatName, outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Problem scaling", e);
        }
    }

}
