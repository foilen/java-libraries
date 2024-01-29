/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

public class RatioTools {

    /**
     * Get the downscale ratio to fit in the max size. The ratio is based on the longer side.
     *
     * @param currentSide1   the first side
     * @param currentSide2   the second side
     * @param maxLongerSide  the max longer side
     * @param maxShorterSide the max shorter side
     * @return the downscale ratio or 1 if already fit
     */
    public static float getDownScaleByMaxSide(int currentSide1, int currentSide2, int maxLongerSide, int maxShorterSide) {
        long currentLongerSide = Math.max(currentSide1, currentSide2);
        long currentShorterSide = Math.min(currentSide1, currentSide2);

        float downScale = 1;
        if (currentLongerSide > maxLongerSide) {
            downScale = (float) maxLongerSide / currentLongerSide;
        }

        if (currentShorterSide * downScale > maxShorterSide) {
            downScale = (float) maxShorterSide / currentShorterSide;
        }

        return downScale;
    }

    /**
     * Get the downscale ratio to fit in the max height.
     *
     * @param currentWidth  the width
     * @param currentHeight the height
     * @param maxHeight     the max height
     * @return the downscale ratio or 1 if already fit
     */
    public static float getDownScaleByMaxHeight(int currentWidth, int currentHeight, int maxHeight) {
        float downScale = 1;
        if (currentHeight > maxHeight) {
            downScale = (float) maxHeight / currentHeight;
        }

        return downScale;
    }

    /**
     * Get the downscale ratio to fit in the max width.
     *
     * @param currentWidth  the width
     * @param currentHeight the height
     * @param maxWitdh      the max width
     * @return the downscale ratio or 1 if already fit
     */
    public static float getDownScaleByMaxWidth(int currentWidth, int currentHeight, int maxWitdh) {
        float downScale = 1;
        if (currentWidth > maxWitdh) {
            downScale = (float) maxWitdh / currentWidth;
        }

        return downScale;
    }


}
