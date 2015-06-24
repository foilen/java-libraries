/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.math;

/**
 * Some math utils.
 */
public class MathUtils {

    /**
     * Tells if the signs are different.
     * 
     * @param a
     *            first item
     * @param b
     *            second item
     * @return true if different
     */
    static public boolean hasDifferentSigns(float a, float b) {
        return ((a < 0) && (b > 0)) || ((a > 0) && (b < 0));
    }

    /**
     * Give the position where to insert the drawing on a canvas to be centered.
     * 
     * @param drawingSize
     *            the size (width or height) of the drawing to put on the canvas
     * @param canvasSize
     *            the size (width or height) of the canvas
     * @return the position
     */
    static public float positionForCenter(float drawingSize, float canvasSize) {
        return (canvasSize - drawingSize) / 2;
    }
}
