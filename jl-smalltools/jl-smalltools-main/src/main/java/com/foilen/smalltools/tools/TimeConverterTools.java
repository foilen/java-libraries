package com.foilen.smalltools.tools;

/**
 * To help converting milliseconds in a long format.
 */
public class TimeConverterTools {

    static private final long SECOND = 1000L;
    static private final long MINUTE = 60L * SECOND;
    static private final long HOUR = 60L * MINUTE;
    static private final long DAY = 24L * HOUR;
    static private final long WEEK = 7L * DAY;

    /**
     * Take an amount of milliseconds and transform them to text up to weeks.
     *
     * @param totalInMs the time to convert
     * @return the time like "24s 34ms" or "34w 5d 16h 1m 15s 856ms"
     * @deprecated use {@link #convertToTextFromMs(Long)}
     */
    @Deprecated
    static public String convertToText(Long totalInMs) {
        return convertToTextFromMs(totalInMs);
    }

    /**
     * Take an amount of minutes and transform them to text up to weeks.
     *
     * @param totalInMin the time to convert
     * @return the time like "24s" or "34w 5d 16h 1m"
     */
    static public String convertToTextFromMin(Long totalInMin) {

        StringBuilder text = new StringBuilder();

        if (totalInMin == null) {
            return null;
        }

        long totalInMs = totalInMin * 60000;
        totalInMs = convertToTextTakeSome(totalInMs, WEEK, "w", text);
        totalInMs = convertToTextTakeSome(totalInMs, DAY, "d", text);
        totalInMs = convertToTextTakeSome(totalInMs, HOUR, "h", text);
        totalInMs = convertToTextTakeSome(totalInMs, MINUTE, "m", text);

        if (text.length() == 0) {
            return "0m";
        }

        return text.toString();

    }

    /**
     * Take an amount of milliseconds and transform them to text up to weeks.
     *
     * @param totalInMs the time to convert
     * @return the time like "24s 34ms" or "34w 5d 16h 1m 15s 856ms"
     */
    static public String convertToTextFromMs(Long totalInMs) {

        StringBuilder text = new StringBuilder();

        if (totalInMs == null) {
            return null;
        }

        totalInMs = convertToTextTakeSome(totalInMs, WEEK, "w", text);
        totalInMs = convertToTextTakeSome(totalInMs, DAY, "d", text);
        totalInMs = convertToTextTakeSome(totalInMs, HOUR, "h", text);
        totalInMs = convertToTextTakeSome(totalInMs, MINUTE, "m", text);
        totalInMs = convertToTextTakeSome(totalInMs, SECOND, "s", text);
        totalInMs = convertToTextTakeSome(totalInMs, 1L, "ms", text);

        if (text.length() == 0) {
            return "0ms";
        }

        return text.toString();

    }

    /**
     * Take an amount of seconds and transform them to text up to weeks.
     *
     * @param totalInSec the time to convert
     * @return the time like "24s" or "34w 5d 16h 1m 15s"
     */
    static public String convertToTextFromSec(Long totalInSec) {

        StringBuilder text = new StringBuilder();

        if (totalInSec == null) {
            return null;
        }

        long totalInMs = totalInSec * 1000;
        totalInMs = convertToTextTakeSome(totalInMs, WEEK, "w", text);
        totalInMs = convertToTextTakeSome(totalInMs, DAY, "d", text);
        totalInMs = convertToTextTakeSome(totalInMs, HOUR, "h", text);
        totalInMs = convertToTextTakeSome(totalInMs, MINUTE, "m", text);
        totalInMs = convertToTextTakeSome(totalInMs, SECOND, "s", text);

        if (text.length() == 0) {
            return "0s";
        }

        return text.toString();

    }

    static private long convertToTextTakeSome(long totalInMs, long trancheSize, String trancheName, StringBuilder text) {
        long tranche = totalInMs / trancheSize;
        if (tranche > 0) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(tranche).append(trancheName);
            totalInMs -= tranche * trancheSize;
        } else {
            if (text.length() > 0) {
                text.append(" 0").append(trancheName);
            }
        }
        return totalInMs;
    }

}
