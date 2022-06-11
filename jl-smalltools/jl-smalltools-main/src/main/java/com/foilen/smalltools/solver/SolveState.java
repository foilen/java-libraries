/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.solver;

public enum SolveState {

    NO_ADDMORE, // Not yet a result. Add more items.
    NO_WONTBE, // Not yet a result. Adding more items won't solve it, so ending that path.
    YES, // Is a result.

}
