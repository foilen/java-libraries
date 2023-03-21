/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tuple;

/**
 * A tuple of two values.
 */
public class Tuple2<A, B> {

    private A a;
    private B b;

    /**
     * Create empty.
     */
    public Tuple2() {
    }

    /**
     * Create with values.
     *
     * @param a the first value
     * @param b the second value
     */
    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
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
        Tuple2 other = (Tuple2) obj;
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
        return true;
    }

    /**
     * Get the first value.
     *
     * @return the first value
     */
    public A getA() {
        return a;
    }

    /**
     * Get the second value.
     *
     * @return the second value
     */
    public B getB() {
        return b;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        return result;
    }

    /**
     * Set the first value.
     *
     * @param a the first value
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * Set the second value.
     *
     * @param b the second value
     */
    public void setB(B b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Tuple2 [a=" + a + ", b=" + b + "]";
    }

}
