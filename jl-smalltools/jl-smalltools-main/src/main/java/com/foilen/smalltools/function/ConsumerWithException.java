/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.function;

import java.util.function.Consumer;

/**
 * Like {@link Consumer}, but that can throw exceptions.
 */
public interface ConsumerWithException<T, E extends Throwable> {

    void accept(T consumer) throws E;

}
