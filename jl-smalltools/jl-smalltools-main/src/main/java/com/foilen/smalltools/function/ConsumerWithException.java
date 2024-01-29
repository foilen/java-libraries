/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.function;

import java.util.function.Consumer;

/**
 * Like {@link Consumer}, but that can throw exceptions.
 */
public interface ConsumerWithException<T, E extends Throwable> {

    /**
     * Performs this operation on the given argument.
     *
     * @param consumer the input argument
     * @throws E any exception
     */
    void accept(T consumer) throws E;

}
