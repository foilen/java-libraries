/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.stream.Stream;

/**
 * Tools to work with streams.
 */
public class StreamTools {

    /**
     * Concat multiple streams together.
     *
     * @param <E>     the type of elements in the stream
     * @param streams the streams
     * @return the concatenated stream
     */
    @SafeVarargs
    public static <E> Stream<E> concat(Stream<E>... streams) {

        int length = streams.length;
        if (length == 0) {
            return Stream.empty();
        }
        if (length == 1) {
            return streams[0];
        }

        Stream<E> totalStream = Stream.concat(streams[0], streams[1]);

        for (int i = 2; i < length; ++i) {
            totalStream = Stream.concat(totalStream, streams[i]);
        }

        return totalStream;

    }

}
