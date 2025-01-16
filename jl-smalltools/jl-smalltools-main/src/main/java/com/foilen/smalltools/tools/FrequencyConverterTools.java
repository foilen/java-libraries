/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * To help converting frequency values to Long.
 */
public class FrequencyConverterTools {

    static public long K = 1000L;
    static public long M = 1000 * K;
    static public long G = 1000 * M;
    static public long T = 1000 * G;

    /**
     * Convert the value to its biggest unit with 2 decimals. (e.g: 123000 will become 123Khz ; 1230000 will become 1.23Mhz)
     *
     * @param hertz the amount of hertz
     * @return the value with its unit
     */
    static public String convertToBiggestHzUnit(Long hertz) {
        if (hertz == null) {
            return null;
        }

        String unit = "Hz";
        double main = hertz;

        if (hertz >= T) {
            unit = "Thz";
            main = main / T;
        } else if (hertz >= G) {
            unit = "Ghz";
            main = main / G;
        } else if (hertz >= M) {
            unit = "Mhz";
            main = main / M;
        } else if (hertz >= K) {
            unit = "Khz";
            main = main / K;
        }

        main *= 100;
        main = Math.round(main) / 100.0;

        String text = String.valueOf(main);
        int dotPos = text.indexOf('.');
        if (dotPos != -1) {
            int maxDecimalPos = Math.min(text.length(), dotPos + 3);
            while (text.charAt(maxDecimalPos - 1) == '.' || text.charAt(maxDecimalPos - 1) == '0') {
                --maxDecimalPos;
                if (text.charAt(maxDecimalPos) == '.') {
                    break;
                }
            }
            text = text.substring(0, maxDecimalPos);
        }

        return text + unit;
    }

}
