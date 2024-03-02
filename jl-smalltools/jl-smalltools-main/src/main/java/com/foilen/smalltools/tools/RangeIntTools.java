/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Help storing values as ranges and checking if a value is in a range.
 */
public class RangeIntTools {

    private List<RangeInt> ranges = new ArrayList<>();

    public void addValue(int value) {
        int smallRangeIndex = -1;
        int bigRangeIndex = -1;
        for (int i = 0; i < ranges.size(); ++i) {
            RangeInt range = ranges.get(i);
            if (range.isInRange(value)) {
                return;
            }
            if (range.getBegin() > value) {
                bigRangeIndex = i;
                break;
            }
            smallRangeIndex = i;
        }
        RangeInt smallRange = smallRangeIndex == -1 ? null : ranges.get(smallRangeIndex);
        RangeInt bigRange = bigRangeIndex == -1 ? null : ranges.get(bigRangeIndex);

        // Check if can extend the small one
        boolean addedToExisting = false;
        if (smallRange != null) {
            if (smallRange.getBegin() - 1 == value) {
                smallRange.setBegin(value);
                addedToExisting = true;
            }
            if (smallRange.getEnd() + 1 == value) {
                smallRange.setEnd(value);
                addedToExisting = true;
            }
        }

        // Check if can extend the big one
        if (bigRange != null) {
            if (bigRange.getBegin() - 1 == value) {
                bigRange.setBegin(value);
                addedToExisting = true;
            }
            if (bigRange.getEnd() + 1 == value) {
                bigRange.setEnd(value);
                addedToExisting = true;
            }
        }

        // Create a new one
        if (!addedToExisting) {
            if (bigRangeIndex == -1) {
                ranges.add(new RangeInt(value, value));
            } else {
                ranges.add(bigRangeIndex, new RangeInt(value, value));
            }
            return;
        }

        // Check if the end of the small is the begin of the big. If that is the case, merge them
        if (smallRange != null && bigRange != null) {
            if (smallRange.getEnd() == bigRange.getBegin()) {
                smallRange.setEnd(bigRange.getEnd());
                ranges.remove(bigRangeIndex);
            }
        }
    }

    public List<RangeInt> getRanges() {
        return ranges;
    }

    public void setRanges(List<RangeInt> ranges) {
        this.ranges = ranges;
    }

    public boolean isInRange(int toCheck) {
        // Check if is in a range and stop when the beginning of the range is higher than the value
        for (RangeInt range : ranges) {
            if (range.isInRange(toCheck)) {
                return true;
            }
            if (range.getBegin() > toCheck) {
                return false;
            }
        }
        return false;
    }
}
