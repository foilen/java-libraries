/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class BeanPropertiesCopierToolsTest {

    public static class DestinationBeanPropertiesCopierTools {
        private String text;
        private String secondText;
        private int number;
        private int secondNumber;
        private Set<String> texts;
        private Set<String> secondTexts;

        public int getNumber() {
            return number;
        }

        public int getSecondNumber() {
            return secondNumber;
        }

        public String getSecondText() {
            return secondText;
        }

        public Set<String> getSecondTexts() {
            return secondTexts;
        }

        public String getText() {
            return text;
        }

        public Set<String> getTexts() {
            return texts;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public void setSecondNumber(int secondNumber) {
            this.secondNumber = secondNumber;
        }

        public void setSecondText(String secondText) {
            this.secondText = secondText;
        }

        public void setSecondTexts(Set<String> secondTexts) {
            this.secondTexts = secondTexts;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setTexts(Set<String> texts) {
            this.texts = texts;
        }

    }

    public static class SourceBeanPropertiesCopierTools {
        private String text;
        private int number;
        private List<String> texts;
        private List<String> secondTexts;

        public int getNumber() {
            return number;
        }

        public List<String> getSecondTexts() {
            return secondTexts;
        }

        public String getText() {
            return text;
        }

        public List<String> getTexts() {
            return texts;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public void setSecondTexts(List<String> secondTexts) {
            this.secondTexts = secondTexts;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setTexts(List<String> texts) {
            this.texts = texts;
        }

    }

    private void assertCollectionNoOrder(Collection<String> collection, String... values) {
        Assert.assertNotNull(collection);
        for (String value : values) {
            Assert.assertTrue(collection.contains(value));
        }
        Assert.assertEquals(values.length, collection.size());
    }

    private Set<String> newHashSet(String... values) {
        Set<String> result = new HashSet<>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    @Test
    public void testCopyProperty1() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setText("Hello World");
        source.setNumber(5);
        source.setTexts(Arrays.asList("One", "Two"));
        DestinationBeanPropertiesCopierTools destination = new DestinationBeanPropertiesCopierTools();
        destination.setNumber(10);

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.copyProperty("text");
        copierTools.copyProperty("number");

        Assert.assertEquals("Hello World", destination.getText());
        Assert.assertEquals(5, destination.getNumber());
    }

    @Test
    public void testCopyProperty1_List() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setTexts(Arrays.asList("One", "Two"));
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.copyProperty("texts");

        Assert.assertEquals(Arrays.asList("One", "Two"), destination.getTexts());
    }

    @Test
    public void testCopyProperty1_Null() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();
        destination.setText("Hello World");
        destination.setTexts(Arrays.asList("One", "Two"));

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.copyProperty("text");
        copierTools.copyProperty("texts");

        Assert.assertNull(destination.getText());
        Assert.assertNull(destination.getTexts());
    }

    @Test
    public void testCopyProperty2() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setText("Hello World");
        source.setNumber(5);
        source.setTexts(Arrays.asList("One", "Two"));
        DestinationBeanPropertiesCopierTools destination = new DestinationBeanPropertiesCopierTools();
        destination.setNumber(10);

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.copyProperty("text", "secondText");
        copierTools.copyProperty("number", "secondNumber");

        Assert.assertNull(destination.getText());
        Assert.assertEquals("Hello World", destination.getSecondText());
        Assert.assertEquals(10, destination.getNumber());
        Assert.assertEquals(5, destination.getSecondNumber());
    }

    @Test
    public void testCopyProperty2_List() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setTexts(Arrays.asList("One", "Two"));
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.copyProperty("texts", "secondTexts");

        Assert.assertNull(destination.getTexts());
        Assert.assertEquals(Arrays.asList("One", "Two"), destination.getSecondTexts());
    }

    @Test
    public void testUpdateCollection1ListToList() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setTexts(Arrays.asList("AAA", "BBB", "CCC"));
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();
        destination.setTexts(new ArrayList<>(Arrays.asList("EEE", "BBB")));

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.updateCollection("texts");

        Assert.assertEquals(Arrays.asList("AAA", "BBB", "CCC"), destination.getTexts());
    }

    @Test
    public void testUpdateCollection1ListToSet() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setTexts(Arrays.asList("AAA", "BBB", "CCC"));
        DestinationBeanPropertiesCopierTools destination = new DestinationBeanPropertiesCopierTools();
        destination.setTexts(newHashSet("EEE", "BBB"));

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.updateCollection("texts");

        assertCollectionNoOrder(destination.getTexts(), "AAA", "BBB", "CCC");
    }

    @Test
    public void testUpdateCollection1SetToList() {
        DestinationBeanPropertiesCopierTools source = new DestinationBeanPropertiesCopierTools();
        source.setTexts(newHashSet("AAA", "BBB", "CCC"));
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();
        destination.setTexts(new ArrayList<>(Arrays.asList("EEE", "BBB")));

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.updateCollection("texts");

        assertCollectionNoOrder(destination.getTexts(), "AAA", "BBB", "CCC");
    }

    @Test
    public void testUpdateCollection1SourceListNull() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        SourceBeanPropertiesCopierTools destination = new SourceBeanPropertiesCopierTools();
        destination.setTexts(new ArrayList<>(Arrays.asList("EEE", "BBB")));

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.updateCollection("texts");

        Assert.assertEquals(Arrays.asList(), destination.getTexts());
    }

    @Test
    public void testUpdateCollection2() {
        SourceBeanPropertiesCopierTools source = new SourceBeanPropertiesCopierTools();
        source.setTexts(Arrays.asList("AAA", "BBB", "CCC"));
        DestinationBeanPropertiesCopierTools destination = new DestinationBeanPropertiesCopierTools();

        BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(source, destination);
        copierTools.updateCollection("texts", "secondTexts");

        Assert.assertNull(destination.getTexts());
        assertCollectionNoOrder(destination.getSecondTexts(), "AAA", "BBB", "CCC");

    }

}
