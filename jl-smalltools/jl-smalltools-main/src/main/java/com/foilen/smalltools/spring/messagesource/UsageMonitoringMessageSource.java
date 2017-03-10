/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.spring.messagesource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.trigger.SmoothTrigger;
import com.google.common.base.Joiner;

public class UsageMonitoringMessageSource implements MessageSource {

    private static final Logger logger = LoggerFactory.getLogger(UsageMonitoringMessageSource.class);

    private String basename;
    private File tmpUsed;

    private SmoothTrigger smoothTrigger;
    private Object lock = new Object();

    // Codes
    private Map<Locale, File> filePerLocale = new HashMap<>();
    private Map<Locale, Map<String, String>> messagesPerLocale = new HashMap<>();
    private Set<String> allCodesInFiles = new HashSet<>();

    // Known used codes
    private Set<String> knownUsedCodes = new HashSet<>();

    public UsageMonitoringMessageSource(String basename) {
        this.basename = basename;

        init();
    }

    private void addKnownUsedCode(String code) {
        synchronized (lock) {

            if (knownUsedCodes.add(code)) {

                // Is a new, make sure it is present everywhere
                String anyValue = findAnyValue(code);

                // Generate it if does not exists
                if (anyValue == null) {
                    anyValue = "!" + code + "!";
                }

                // Check that it is present in all the locales
                for (Map<String, String> messages : messagesPerLocale.values()) {
                    if (!messages.containsKey(code)) {
                        messages.put(code, anyValue);
                    }
                }

                // Request a save
                smoothTrigger.request();
            }

        }
    }

    private String findAnyValue(String missingCode) {
        for (Map<String, String> messages : messagesPerLocale.values()) {
            String codeValue = messages.get(missingCode);
            if (codeValue != null) {
                return codeValue;
            }
        }

        return null;
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        throw new SmallToolsException("Not implemented yet");
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        addKnownUsedCode(code);
        String value = messagesPerLocale.get(locale).get(code);
        return String.format(value, args);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        addKnownUsedCode(code);
        String value = messagesPerLocale.get(locale).get(code);
        return String.format(value, args);
    }

    private void init() {

        // Check the base folder
        File basenameFile = new File(basename);
        logger.info("Base name is {}", basename);
        File directory = basenameFile.getParentFile();
        logger.info("Base directory is {}", directory.getAbsoluteFile());
        if (!directory.exists()) {
            throw new SmallToolsException("Directory: " + directory.getAbsolutePath() + " does not exists");
        }

        tmpUsed = new File(directory.getAbsolutePath() + File.separatorChar + "_messages_usage.txt");

        // Check the files in it
        String startswith = basenameFile.getName() + "_";
        String endswith = ".properties";
        for (File file : directory.listFiles((FilenameFilter) (dir, name) -> name.startsWith(startswith) && name.endsWith(endswith))) {
            // Create the locale
            logger.info("Found messages file {}", directory.getAbsoluteFile());
            String filename = file.getName();
            String localePart = filename.substring(startswith.length(), filename.length() - endswith.length());
            Locale locale = new Locale(localePart);
            logger.info("Locale is {} -> {}", localePart, locale);
            filePerLocale.put(locale, file);

            // Load the file
            Properties properties = new Properties();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                properties.load(new InputStreamReader(inputStream, CharsetTools.UTF_8));
            } catch (IOException e) {
                logger.error("Problem reading the property file {}", file.getAbsoluteFile(), e);
                throw new SmallToolsException("Problem reading the file", e);
            }

            // Check codes and save values
            Map<String, String> messages = new HashMap<>();
            messagesPerLocale.put(locale, messages);
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = properties.getProperty(name);
                allCodesInFiles.add(name);
                messages.put(name, value);
            }

        }

        // Add missing codes in all the maps (copy one that has it)
        for (Locale locale : filePerLocale.keySet()) {
            Set<String> missingCodes = new HashSet<>();
            Map<String, String> messagesForCurrentLocale = messagesPerLocale.get(locale);

            // Get the ones missing
            missingCodes.addAll(allCodesInFiles);
            missingCodes.removeAll(messagesForCurrentLocale.keySet());

            for (String missingCode : missingCodes) {
                logger.info("Locale {} was missing code {}", locale, missingCode);

                String codeValue = findAnyValue(missingCode);
                messagesForCurrentLocale.put(missingCode, codeValue);
            }
        }

        // Load the already known codes
        if (tmpUsed.exists()) {
            for (String line : FileTools.readFileLinesIteration(tmpUsed.getAbsolutePath())) {
                knownUsedCodes.add(line);
            }
        }

        smoothTrigger = new SmoothTrigger(() -> {

            synchronized (lock) {

                logger.info("Begin saving locale files");

                // Go through each locale
                for (Entry<Locale, File> entry : filePerLocale.entrySet()) {
                    Map<String, String> messages = messagesPerLocale.get(entry.getKey());

                    try (PrintWriter printWriter = new PrintWriter(entry.getValue(), CharsetTools.UTF_8.toString())) {

                        // Save the known used (sorted) at the top
                        for (String code : knownUsedCodes.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
                            printWriter.println(code + "=" + messages.get(code));
                        }
                        printWriter.println();

                        // Save the others (sorted) at the bottom
                        Set<String> unknownCodes = new HashSet<>();
                        unknownCodes.addAll(messages.keySet());
                        unknownCodes.removeAll(knownUsedCodes);
                        if (!unknownCodes.isEmpty()) {
                            printWriter.println("# Unknown");
                            printWriter.println();

                            for (String code : unknownCodes.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
                                printWriter.println(code + "=" + messages.get(code));
                            }
                            printWriter.println();
                        }
                    } catch (Exception e) {
                        logger.error("Could not write the file", e);
                    }
                }

                // Save the known
                FileTools.writeFile(Joiner.on('\n').join(knownUsedCodes.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())), tmpUsed);

                logger.info("Done saving locale files");
            }

        }) //
                .setDelayAfterLastTriggerMs(5000) //
                .setMaxDelayAfterFirstRequestMs(10000) //
                .setFirstPassThrough(true) //
                .start();

        smoothTrigger.request();
    }

}
