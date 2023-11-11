/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tuple;

import com.google.common.collect.ComparisonChain;

/**
 * A tuple of 3 values.
 */
public class Tuple3<A, B, C> implements Comparable<Tuple3<A, B, C>> {

    private A a;
    private B b;
    private C c;

    /**
     * Create empty.
     */
    public Tuple3() {
    }

    /**
     * Create with values.
     *
     * @param a the first value
     * @param b the second value
     * @param c the third value
     */
    public Tuple3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple3 other = (Tuple3) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        if (c == null) {
            if (other.c != null) {
                return false;
            }
        } else if (!c.equals(other.c)) {
            return false;
        }
        return true;
    }

    /**
     * Get the first value.
     *
     * @return the value
     */
    public A getA() {
        return a;
    }

    /**
     * Get the second value.
     *
     * @return the value
     */
    public B getB() {
        return b;
    }

    /**
     * Get the third value.
     *
     * @return the value
     */
    public C getC() {
        return c;
    }

    @Override
    public int compareTo(Tuple3<A, B, C> o) {
        return ComparisonChain.start()
                .compare(a, o.a, TupleTools::nullComparator)
                .compare(b, o.b, TupleTools::nullComparator)
                .compare(c, o.c, TupleTools::nullComparator)
                .result();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        result = prime * result + ((c == null) ? 0 : c.hashCode());
        return result;
    }

    /**
     * Set the first value.
     *
     * @param a the value
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * Set the second value.
     *
     * @param b the value
     */
    public void setB(B b) {
        this.b = b;
    }

    /**
     * Set the third value.
     *
     * @param c the value
     */
    public void setC(C c) {
        this.c = c;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tuple3 [a=");
        builder.append(a);
        builder.append(", b=");
        builder.append(b);
        builder.append(", c=");
        builder.append(c);
        builder.append("]");
        return builder.toString();
    }

}
