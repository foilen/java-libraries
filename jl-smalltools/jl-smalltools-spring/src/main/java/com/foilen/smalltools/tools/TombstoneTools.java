/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * When you want to check if some code is dead or some deprecated API is no more used, you can call this Tombstone and then check the logs to see if it is still being used. The date is just for you to
 * know if that made enough time that you are monitoring that code.
 *
 * <p>
 * See details <a href="https://youtu.be/29UXzfQWOhQ">in this video</a>
 * </p>
 */
public final class TombstoneTools {

    private final static Logger logger = LoggerFactory.getLogger(TombstoneTools.class);

    /**
     * Add a warning log entry.
     *
     * @param id   the id to output in the log (should be unique to know which one was used)
     * @param time the time when you added that entry (could be a date or the version number)
     */
    public static void log(String id, String time) {
        logger.warn("{}", id);
    }

    /**
     * Add a warning log entry telling which user in Spring Security is using it.
     *
     * @param id   the id to output in the log (should be unique to know which one was used)
     * @param time the time when you added that entry (could be a date or the version number)
     */
    public static void logWithUser(String id, String time) {

        SecurityContext securityContext = null;
        try {
            securityContext = SecurityContextHolder.getContext();
        } catch (Exception e) {
        }

        String user = null;
        Class<? extends Authentication> authClass = null;
        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                authClass = authentication.getClass();
                user = authentication.getName();
            }
        }

        logger.warn("{} - AuthClass: {} - User: {}", id, authClass, user);
    }

    private TombstoneTools() {
    }

}
