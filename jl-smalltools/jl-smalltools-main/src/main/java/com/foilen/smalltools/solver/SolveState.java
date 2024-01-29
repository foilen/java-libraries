/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.solver;

/**
 * The state of the solver.
 */
public enum SolveState {

    /**
     * Not yet a result. Add more items.
     */
    NO_ADDMORE,
    /**
     * Not yet a result. Adding more items won't solve it, so ending that path.
     */
    NO_WONTBE,
    /**
     * Is a result.
     */
    YES,

}
