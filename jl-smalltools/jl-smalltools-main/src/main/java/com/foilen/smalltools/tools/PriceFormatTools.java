package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Strings;

/**
 * To ensure correct decimal value, you can use a long to store the price. For a price like AAA.BB, you would store in a long the value AAABB.
 */
public class PriceFormatTools {

    /**
     * Convert a long in the form AAABB to a text AAA.BB .
     *
     * @param price the price
     * @return the formatted price
     */
    public static String toDigit(long price) {
        long a = price / 100;
        long b = Math.abs(price % 100);

        String decimal = String.valueOf(b);
        if (decimal.length() == 1) {
            decimal = "0" + decimal;
        }

        return a + "." + decimal;
    }

    /**
     * Given a price in the format AAA.BB , convert it to a long value of AAABB .
     *
     * @param text the text
     * @return the long
     */
    public static long toLong(String text) {

        if (Strings.isNullOrEmpty(text)) {
            return 0;
        }

        int dotPosition = text.indexOf(".");
        int comaPosition = text.indexOf(",");
        if (dotPosition != -1 && comaPosition != -1) {
            throw new SmallToolsException("Wrong price format");
        }

        int pos = Math.max(comaPosition, dotPosition);

        boolean hasDot = pos != -1;
        if (!hasDot) {
            pos = text.length();
        }

        // Get the parts
        String first = text.substring(0, pos);
        String second = "";
        if (hasDot) {
            second = text.substring(pos + 1);
            if (second.length() > 2) {
                throw new SmallToolsException("Wrong price format");
            }
        }
        long a = 0;
        try {
            if (!Strings.isNullOrEmpty(first)) {
                a = Long.valueOf(first);
            }
        } catch (Exception e) {
            throw new SmallToolsException("Wrong price format");
        }

        long b = 0;
        try {
            if (!Strings.isNullOrEmpty(second)) {
                while (second.length() < 2) {
                    second += '0';
                }

                b = Long.valueOf(second);
            }
        } catch (Exception e) {
            throw new SmallToolsException("Wrong price format");
        }

        return a * 100 + b;
    }
}
