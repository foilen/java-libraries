package com.foilen.smalltools.reflection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.foilen.smalltools.inputstream.ZerosInputStream;

public class ReflectionToolsTest {

    private static interface Animal {
        public int getAge();

        public String getName();
    }

    private static class AnimalContainer {
        private Animal animal;

        public Animal getAnimal() {
            return animal;
        }

        public void setAnimal(Animal animal) {
            this.animal = animal;
        }
    }

    private static class Anything {
        private String name;
        private int age;
        @MyAnnotation
        private Dog animal;

        public int getAge() {
            return age;
        }

        public Dog getAnimal() {
            return animal;
        }

        @MyAnnotation
        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setAnimal(Dog animal) {
            this.animal = animal;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @SuppressWarnings("unused")
    private static class Anything2 extends Anything {
        private String nickname;
        @MyAnnotation
        private String something;

        @MyAnnotation
        private void init() {
        }
    }

    @SuppressWarnings("unused")
    private static class Cat implements Animal {
        private String name;
        private int age;

        @Override
        public int getAge() {
            return age;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    private static class Dog implements Animal {
        private String name;
        private int age;

        @Override
        public int getAge() {
            return age;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface MyAnnotation {
    }

    private static class PartialProperties {

        private String name;

        public String getName() {
            return name;
        }

        public String getSpecialName() {
            return name + "YaY";
        }

        public void setName(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public void setNothing(String nothing) {
        }

    }

    private void assertContainField(List<Field> fields, String fieldName) {
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                return;
            }
        }
        Assertions.fail("Field " + fieldName + " is not part of the list");
    }

    private void assertContainMethod(List<Method> methods, String methodName) {
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return;
            }
        }
        Assertions.fail("Method " + methodName + " is not part of the list");
    }

    @Test
    public void testAllFields() {
        List<Field> fields = ReflectionTools.allFields(Anything2.class);
        int expectedSize = 5;
        if (fields.size() == expectedSize + 2) {
            // Coverage test
            assertContainField(fields, "$jacocoData");
        } else {
            Assertions.assertEquals(expectedSize, fields.size());
        }
        assertContainField(fields, "age");
        assertContainField(fields, "animal");
        assertContainField(fields, "name");
        assertContainField(fields, "nickname");
        assertContainField(fields, "something");
    }

    @Test
    public void testAllFieldsWithAnnotation() {
        List<Field> fields = ReflectionTools.allFieldsWithAnnotation(Anything2.class, MyAnnotation.class);
        Assertions.assertEquals(2, fields.size());
        assertContainField(fields, "animal");
        assertContainField(fields, "something");
    }

    @Test
    public void testAllMethodsWithAnnotations() {
        List<Method> methods = ReflectionTools.allMethodsWithAnnotation(Anything2.class, MyAnnotation.class);
        Assertions.assertEquals(2, methods.size());
        assertContainMethod(methods, "getName");
        assertContainMethod(methods, "init");
    }

    @Test
    public void testCopyAllProperties() {

        // Simple
        Dog dog = new Dog();
        Anything anything = new Anything();
        dog.setAge(10);
        dog.setName("Fido");
        ReflectionTools.copyAllProperties(dog, anything);
        Assertions.assertEquals(10, anything.getAge());
        Assertions.assertEquals("Fido", anything.getName());
        Assertions.assertNull(anything.getAnimal());

        // Simple reverse
        dog = new Dog();
        Dog setDog = new Dog();
        anything.setAge(20);
        anything.setName("Rick");
        anything.setAnimal(setDog);
        ReflectionTools.copyAllProperties(anything, dog);
        Assertions.assertEquals(20, dog.getAge());
        Assertions.assertEquals("Rick", dog.getName());

        // With sub-class
        AnimalContainer animalContainer = new AnimalContainer();
        ReflectionTools.copyAllProperties(anything, animalContainer);
        Assertions.assertEquals(setDog, animalContainer.getAnimal());

        // With sub-class (wrong way)
        anything = new Anything();
        animalContainer.setAnimal(new Cat());
        ReflectionTools.copyAllProperties(animalContainer, anything);
        Assertions.assertEquals(0, anything.getAge());
        Assertions.assertNull(anything.getName());
        Assertions.assertNull(anything.getAnimal());

        // Partial Properties
        PartialProperties partial = new PartialProperties();
        partial.setName("Joe");
        PartialProperties copiedPartial = new PartialProperties();
        ReflectionTools.copyAllProperties(partial, copiedPartial);
        Assertions.assertEquals("Joe", copiedPartial.getName());
        Assertions.assertEquals("JoeYaY", copiedPartial.getSpecialName());
    }

    @Test
    public void testInstantiateEmptyContructor() {
        AtomicLong actual = ReflectionTools.instantiate(AtomicLong.class);
        Assertions.assertNotNull(actual);
    }

    @Test
    public void testInstantiateNonEmptyContructor() {
        ZerosInputStream actual = ReflectionTools.instantiate(ZerosInputStream.class, 10L);
        Assertions.assertNotNull(actual);
    }

}
