package com.foilen.smalltools.test.asserts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;

import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tools.SystemTools;

/**
 * Assertions.
 */
public final class AssertTools {

    /**
     * Diff the initial vs final items (by comparing their JSON dump) and output as JSON dump (by their JSON dump).
     *
     * @param expected     the expected object to compare
     * @param initialItems the initial items
     * @param finalItems   the final items
     */
    public static void assertDiffJsonComparison(AssertDiff expected, List<?> initialItems, List<?> finalItems) {
        assertJsonComparison(expected, diffLists(initialItems, finalItems));
    }

    /**
     * Load an expected object from a JSON resource file and diff the initial vs final items (by comparing their JSON dump) and output as JSON dump (by their JSON dump).
     * <p>
     * <p>
     * You can set the system property "ASSERT_TOOLS_UPDATE_EXPECTED_FILE" to "true" or some part of the path to let the tool update the file with the "actual" json.
     *
     * @param expectedResource the filename of the resource
     * @param expectedContext  the class in which the resource file is
     * @param initialItems     the initial items
     * @param finalItems       the final items
     */
    public static void assertDiffJsonComparison(String expectedResource, Class<?> expectedContext, List<?> initialItems, List<?> finalItems) {
        assertJsonComparison(expectedResource, expectedContext, diffLists(initialItems, finalItems));
    }

    /**
     * Diff the initial vs final items (by comparing their JSON dump) and output as JSON dump (by their JSON dump ignoring nulls).
     *
     * @param expected     the expected object to compare
     * @param initialItems the initial items
     * @param finalItems   the final items
     */
    public static void assertDiffJsonComparisonWithoutNulls(Object expected, List<?> initialItems, List<?> finalItems) {
        assertJsonComparisonWithoutNulls(expected, diffLists(initialItems, finalItems));
    }

    /**
     * Load an expected object from a JSON resource file and diff the initial vs final items (by comparing their JSON dump) and output as JSON dump (by their JSON dump ignoring nulls).
     * <p>
     * You can set the system property "ASSERT_TOOLS_UPDATE_EXPECTED_FILE" to "true" or some part of the path to let the tool update the file with the "actual" json.
     *
     * @param expectedResource the filename of the resource
     * @param expectedContext  the class in which the resource file is
     * @param initialItems     the initial items
     * @param finalItems       the final items
     */
    public static void assertDiffJsonComparisonWithoutNulls(String expectedResource, Class<?> expectedContext, List<?> initialItems, List<?> finalItems) {
        assertJsonComparisonWithoutNulls(expectedResource, expectedContext, diffLists(initialItems, finalItems));
    }

    /**
     * Assert expected - delta &lt;= actual &lt;= expected + delta .
     *
     * @param expected the center of the expected value
     * @param actual   the actual value to assert
     * @param delta    the delta between the expected value
     */
    public static void assertEqualsDelta(int expected, int actual, int delta) {
        if (Math.abs(expected - actual) > delta) {
            long expectedLow = expected - delta;
            long expectedHigh = expected + delta;
            Assert.fail("Expecting value between " + expectedLow + " and " + expectedHigh + ", but got " + actual);
        }
    }

    /**
     * Assert expected - delta &lt;= actual &lt;= expected + delta .
     *
     * @param expected the center of the expected value
     * @param actual   the actual value to assert
     * @param delta    the delta between the expected value
     */
    public static void assertEqualsDelta(long expected, long actual, long delta) {
        if (Math.abs(expected - actual) > delta) {
            long expectedLow = expected - delta;
            long expectedHigh = expected + delta;
            Assert.fail("Expecting value between " + expectedLow + " and " + expectedHigh + ", but got " + actual);
        }
    }

    /**
     * Assert the content inside files.
     *
     * @param expectedFile the expected file
     * @param actualFile   the actual file
     */
    public static void assertFileContent(File expectedFile, File actualFile) {
        // Check the file size
        Assert.assertEquals(expectedFile.length(), actualFile.length());

        // Check the files
        try {
            assertStreamContent(new FileInputStream(expectedFile), new FileInputStream(actualFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Issue opening the files", e);
        }

    }

    /**
     * Assert the content inside files.
     *
     * @param expectedStream the expected content
     * @param actualFile     the actual file name
     */
    public static void assertFileContent(InputStream expectedStream, String actualFile) {
        // Check the files
        try {
            assertStreamContent(expectedStream, new FileInputStream(actualFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Issue opening the file", e);
        }
    }

    /**
     * Assert text content ignoring line feeds.
     *
     * @param expected the expected text
     * @param actual   the actual text
     */
    public static void assertIgnoreLineFeed(String expected, String actual) {
        Assert.assertEquals(expected.replaceAll("\r", ""), actual.replaceAll("\r", ""));
    }

    /**
     * Compare the expected object to the actual object (by their JSON dump).
     *
     * @param expected the expected object to compare
     * @param actual   the actual object to compare to
     */
    public static void assertJsonComparison(Object expected, Object actual) {
        String expectedJson = JsonTools.prettyPrint(expected);
        String actualJson = JsonTools.prettyPrint(actual);

        assertIgnoreLineFeed(expectedJson, actualJson);
    }

    /**
     * Load an expected object from a JSON resource file and compare it to the actual object (by their JSON dump).
     * <p>
     * <p>
     * You can set the system property "ASSERT_TOOLS_UPDATE_EXPECTED_FILE" to "true" or some part of the path to let the tool update the file with the "actual" json.
     *
     * @param expectedResource the filename of the resource
     * @param expectedContext  the class in which the resource file is
     * @param actual           the actual object to compare to
     */
    public static void assertJsonComparison(String expectedResource, Class<?> expectedContext, Object actual) {
        String actualJson = JsonTools.prettyPrint(actual);

        if (updateFileIfRequested(expectedResource, expectedContext, actualJson)) {
            String expectedJson = ResourceTools.getResourceAsString(expectedResource, expectedContext);
            assertIgnoreLineFeed(expectedJson, actualJson);
        }
    }

    /**
     * Compare the expected object to the actual object (by their JSON dump ignoring nulls).
     *
     * @param expected the expected object to compare
     * @param actual   the actual object to compare to
     */
    public static void assertJsonComparisonWithoutNulls(Object expected, Object actual) {
        String expectedJson = JsonTools.prettyPrintWithoutNulls(expected);
        String actualJson = JsonTools.prettyPrintWithoutNulls(actual);

        assertIgnoreLineFeed(expectedJson, actualJson);
    }

    /**
     * Load an expected object from a JSON resource file and compare it to the actual object (by their JSON dump ignoring nulls).
     * <p>
     * You can set the system property "ASSERT_TOOLS_UPDATE_EXPECTED_FILE" to "true" or some part of the path to let the tool update the file with the "actual" json.
     *
     * @param expectedResource the filename of the resource
     * @param expectedContext  the class in which the resource file is
     * @param actual           the actual object to compare to
     */
    public static void assertJsonComparisonWithoutNulls(String expectedResource, Class<?> expectedContext, Object actual) {
        String actualJson = JsonTools.prettyPrintWithoutNulls(actual);

        if (updateFileIfRequested(expectedResource, expectedContext, actualJson)) {
            String expectedJson = ResourceTools.getResourceAsString(expectedResource, expectedContext);
            assertIgnoreLineFeed(expectedJson, actualJson);
        }
    }

    /**
     * Assert the content inside streams.
     *
     * @param expectedStream the expected content
     * @param actualStream   the actual content
     */
    public static void assertStreamContent(InputStream expectedStream, InputStream actualStream) {

        // Check the content
        long position = 0;
        try {

            byte[] expectedBytes = new byte[1024];
            int expectedLen;
            byte[] actualBytes = new byte[1024];
            int actualLen;

            do {
                expectedLen = expectedStream.read(expectedBytes);
                actualLen = actualStream.read(actualBytes);

                Assert.assertEquals(expectedLen, actualLen);
                Assert.assertArrayEquals("Position: " + position, expectedBytes, actualBytes);

                position += expectedLen;
            } while (expectedLen != -1);

        } catch (Exception e) {
            throw new SmallToolsException("Issue copying the stream", e);
        } finally {
            // Close the sources
            try {
                expectedStream.close();
            } catch (Exception e) {
            }
            try {
                actualStream.close();
            } catch (Exception e) {
            }
        }
    }

    private static AssertDiff diffLists(List<?> initialItems, List<?> finalItems) {

        // Transform items to json
        List<String> initialJson = initialItems.stream()
                .map(JsonTools::prettyPrint)
                .collect(CollectionsTools.collectToArrayList());
        List<String> finalJson = finalItems.stream()
                .map(JsonTools::prettyPrint)
                .collect(CollectionsTools.collectToArrayList());

        // Diff
        AssertDiff diff = new AssertDiff();
        for (int i = 0; i < initialJson.size(); ++i) {
            String left = initialJson.get(i);
            if (!finalJson.contains(left)) {
                diff.getRemoved().add(initialItems.get(i));
            }
        }
        for (int i = 0; i < finalJson.size(); ++i) {
            String right = finalJson.get(i);
            if (!initialJson.contains(right)) {
                diff.getAdded().add(finalItems.get(i));
            }
        }
        return diff;
    }

    /**
     * Update the resource file if requested.
     *
     * @param expectedResource the expected resource
     * @param expectedContext  the expected class
     * @param actualJson       the actual
     * @return true if should assert (when no need to update the resource file)
     */
    private static boolean updateFileIfRequested(String expectedResource, Class<?> expectedContext, String actualJson) {
        String updateExpectedFileContains = SystemTools.getPropertyOrEnvironment("ASSERT_TOOLS_UPDATE_EXPECTED_FILE", "false");
        if (!"false".equals(updateExpectedFileContains)) {
            URL url = expectedContext.getResource(expectedResource);
            if (url == null) {
                Assert.fail("The file must already exists (you can create an empty file)");
            } else {
                String filename = url.toString().substring(5);
                String filePart = new File(filename).getName();
                List<String> availableFiles = DirectoryTools.listFilesAndFoldersRecursively(JavaEnvironmentValues.getWorkingDirectory(), true).stream()
                        .filter(it -> it.endsWith(filePart) && !it.equals(filename))
                        .filter(it -> "true".equals(updateExpectedFileContains) || it.contains(updateExpectedFileContains))
                        .toList();

                if (availableFiles.isEmpty()) {
                    Assert.fail("No file found for " + filePart);
                }
                if (availableFiles.size() > 1) {
                    System.out.println("Too many files found for " + filePart + ":");
                    availableFiles.forEach(System.out::println);
                    System.out.println("You can configure ASSERT_TOOLS_UPDATE_EXPECTED_FILE to filter the path containing that value");
                    Assert.fail("Too many files found for " + filePart);
                }
                String fileToUpdate = availableFiles.get(0);
                System.out.println("Updating " + fileToUpdate);
                FileTools.writeFile(actualJson, fileToUpdate);
            }
            return false;
        }

        return true;
    }

    private AssertTools() {
    }

}
