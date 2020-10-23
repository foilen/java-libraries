/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * To help converting space values to Long.
 *
 * @deprecated use {@link SpaceConverterTools}
 */
@Deprecated
public class SpaceConverterTool {

    static public long KB = 1000L;
    static public long MB = 1000 * 1000L;
    static public long GB = 1000 * 1000 * 1000L;
    static public long TB = 1000 * 1000 * 1000 * 1000L;

    static public long KIB = 1024L;
    static public long MIB = 1024 * 1024L;
    static public long GIB = 1024 * 1024 * 1024L;
    static public long TIB = 1024 * 1024 * 1024 * 1024L;

    /**
     * Convert the value to its biggest unit with 2 decimals. (e.g: 123000 will become 123K ; 1230000 will become 1.23M)
     *
     * @param bytes
     *            the amount of bytes
     * @return the value with its unit
     */
    static public String convertToBiggestBUnit(Long bytes) {
        if (bytes == null) {
            return null;
        }

        String unit = "B";
        double main = bytes;

        if (bytes >= TB) {
            unit = "T";
            main = main / TB;
        } else if (bytes >= GB) {
            unit = "G";
            main = main / GB;
        } else if (bytes >= MB) {
            unit = "M";
            main = main / MB;
        } else if (bytes >= KB) {
            unit = "K";
            main = main / KB;
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

    /**
     * Convert text like "15k", "15 k", "15 kB", "15 kb" to its {@link Long} representation (always bytes; not bits).
     *
     * <ul>
     * <li>Support "B", "kB", "mB", "gB" and "tB" (powers of 1000)</li>
     * <li>Support "B", "kiB", "miB", "giB" and "tiB" (powers of 1024)</li>
     * <li>It is case insensitive, that's why it will never give bits, but always bytes</li>
     * </ul>
     *
     * @param space
     *            the text space
     * @return the amount of bytes
     */
    static public long convertToBytes(String space) {
        boolean inNum = true;
        String spaceLower = space.toLowerCase();
        String numeric = "";
        String unit = "";
        for (int i = 0; i < spaceLower.length(); ++i) {
            char current = spaceLower.charAt(i);
            if (inNum) {
                // The numeric part
                if ((current >= '0' && current <= '9') || current == '.') {
                    numeric += current;
                    continue;
                } else {
                    inNum = false;
                }
            }

            // The unit part
            // Skip spaces
            if (current == '\t' || current == ' ') {
                continue;
            } else {
                unit += current;
            }

        }

        // Convert
        try {
            double value = Double.parseDouble(numeric);
            switch (unit) {
            case "":
            case "b":
                break;
            case "k":
            case "kb":
                value *= KB;
                break;
            case "m":
            case "mb":
                value *= MB;
                break;
            case "g":
            case "gb":
                value *= GB;
                break;
            case "t":
            case "tb":
                value *= TB;
                break;

            case "ki":
            case "kib":
                value *= KIB;
                break;
            case "mi":
            case "mib":
                value *= MIB;
                break;
            case "gi":
            case "gib":
                value *= GIB;
                break;
            case "ti":
            case "tib":
                value *= TIB;
                break;

            default:
                throw new SmallToolsException(space + " is an invalid space");
            }
            return (long) value;
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException(space + " is an invalid space");
        }

    }

}
