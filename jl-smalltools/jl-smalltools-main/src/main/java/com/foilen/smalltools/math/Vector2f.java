/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.math;

/**
 * Group of 2 floats.
 */
public class Vector2f {

    public float x, y;

    public Vector2f() {

    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy the values of the other vector.
     *
     * @param vector
     *            the other vector
     */
    public Vector2f(Vector2f vector) {
        set(vector);
    }

    /**
     * Add the other value to the current one.
     *
     * @param add
     *            the vector to add
     * @return return this object
     */
    public Vector2f addLocal(Vector2f add) {
        x += add.x;
        y += add.y;

        return this;
    }

    /**
     * Multiply the other value with the current one.
     *
     * @param div
     *            the amount to divide with
     * @return return this object
     */
    public Vector2f divideLocal(float div) {
        x /= div;
        y /= div;

        return this;
    }

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Multiply the other value with the current one.
     *
     * @param mult
     *            the amount to mult
     * @return return this object
     */
    public Vector2f multLocal(float mult) {
        x *= mult;
        y *= mult;

        return this;
    }

    /**
     * Modify the values to have the length 1. Will stick to (0,0) if it is already.
     */
    public void normalize() {
        if (!isZero()) {
            divideLocal(length());
        }
    }

    /**
     * Copy the values of the other vector.
     *
     * @param vector
     *            the other vector
     */
    public void set(Vector2f vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Subtract the other value from the current one.
     *
     * @param other
     *            the vector to subtract
     * @return return this object
     */
    public Vector2f subLocal(Vector2f other) {
        x -= other.x;
        y -= other.y;

        return this;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
