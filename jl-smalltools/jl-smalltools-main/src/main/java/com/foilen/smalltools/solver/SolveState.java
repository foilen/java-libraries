package com.foilen.smalltools.solver;

public enum SolveState {

    NO_ADDMORE, // Not yet a result. Add more items.
    NO_WONTBE, // Not yet a result. Adding more items won't solve it, so ending that path.
    YES, // Is a result.

}
