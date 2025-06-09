package com.foilen.smalltools.tools;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Some common methods to manage domains.
 */
public final class DomainTools {

    /**
     * Get all the parts of the domain name.
     *
     * @param domain the domain name
     * @return the parts
     */
    public static String[] getParts(String domain) {
        if (domain == null) {
            return null;
        }
        return domain.split("\\.");
    }

    /**
     * Get all the parts of the domain name as a list.
     *
     * @param domain the domain name
     * @return the parts
     */
    public static List<String> getPartsAsList(String domain) {
        if (domain == null) {
            return null;
        }
        return new ArrayList<>(List.of(getParts(domain)));
    }

    /**
     * Get all the parts of the domain name in the reversed order.
     *
     * @param domain the domain name
     * @return the reversed parts
     */
    public static String[] getReverseParts(String domain) {
        if (domain == null) {
            return null;
        }

        String[] parts = getParts(domain);
        ArrayUtils.reverse(parts);
        return parts;
    }

    /**
     * Get all the parts in reverse order of the domain name as a list.
     *
     * @param domain the domain name
     * @return the reversed parts
     */
    public static List<String> getReversePartsAsList(String domain) {
        if (domain == null) {
            return null;
        }
        return new ArrayList<>(List.of(getReverseParts(domain)));
    }

    /**
     * Get the reversed domain name. E.g test.foilen.com becomes com.foilen.test .
     *
     * @param domain the domain name
     * @return the reversed domain name
     */
    public static String reverse(String domain) {
        if (domain == null) {
            return null;
        }

        return Joiner.on(".").join(getReverseParts(domain));
    }

    private DomainTools() {
    }
}
