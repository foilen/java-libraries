/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.math;

/**
 * Group of 4 floats.
 */
public class Vector4f {
    private float a, b, c, d;

    public Vector4f() {

    }

    public Vector4f(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * @return the a
     */
    public float getA() {
        return a;
    }

    /**
     * @return the b
     */
    public float getB() {
        return b;
    }

    /**
     * @return the c
     */
    public float getC() {
        return c;
    }

    /**
     * @return the d
     */
    public float getD() {
        return d;
    }

    /**
     * @param a
     *            the a to set
     */
    public void setA(float a) {
        this.a = a;
    }

    /**
     * @param b
     *            the b to set
     */
    public void setB(float b) {
        this.b = b;
    }

    /**
     * @param c
     *            the c to set
     */
    public void setC(float c) {
        this.c = c;
    }

    /**
     * @param d
     *            the d to set
     */
    public void setD(float d) {
        this.d = d;
    }

    /**
     * Transform to a 4 items array.
     *
     * @return a float array of ABCD
     */
    public float[] toArray() {
        return new float[] { a, b, c, d };
    }

}
