/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Objects;

public class RangeInt implements Comparable<RangeInt> {

    private int begin;
    private int end;

    public RangeInt() {
    }

    public RangeInt(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public boolean isInRange(int value) {
        return value >= begin && value <= end;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public int compareTo(RangeInt o) {
        int result = Integer.compare(begin, o.begin);
        if (result != 0) {
            return result;
        }
        return Integer.compare(end, o.end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeInt range = (RangeInt) o;
        return begin == range.begin && end == range.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }

    @Override
    public String toString() {
        return "Range{" +
                begin +
                "->" +
                end +
                '}';
    }
}
