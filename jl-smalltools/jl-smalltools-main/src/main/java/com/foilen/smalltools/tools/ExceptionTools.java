/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * Some common methods for exceptions.
 */
public final class ExceptionTools {

    public static String getFullStack(Throwable e) {
        StringBuilder report = new StringBuilder();
        appendStackTrace(0, report, e);
        return report.toString();
    }

    private static void appendStackTrace(int dept, StringBuilder report, Throwable e) {
        if (e == null) {
            return;
        }
        String deptText = "\t".repeat(dept);
        report.append(deptText).append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            report.append(deptText).append("\tat ").append(ste).append("\n");
        }
        appendStackTrace(dept + 1, report, e.getCause());
    }

    private ExceptionTools() {
    }

}
