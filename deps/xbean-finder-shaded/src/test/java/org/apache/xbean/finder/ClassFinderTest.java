/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.acme.bar.AnnType;
import org.acme.bar.Construct;
import org.acme.bar.FullyAnnotated;
import org.acme.bar.Get;
import org.acme.bar.ParamA;
import org.acme.bar.Type;
import org.acme.foo.Blue;
import org.acme.foo.Color;
import org.acme.foo.Deployable;
import org.acme.foo.FamilyHalloween;
import org.acme.foo.FunnyFamilyHalloween;
import org.acme.foo.GenericHoliday;
import org.acme.foo.Green;
import org.acme.foo.Halloween;
import org.acme.foo.Holiday;
import org.acme.foo.Primary;
import org.acme.foo.Property;
import org.acme.foo.Red;
import org.acme.foo.StringGenericHoliday;
import org.acme.foo.Thanksgiving;
import org.acme.foo.ValentinesDay;

/**
 * @author David Blevins
 * @version $Rev$ $Date$
 */
public class ClassFinderTest extends TestCase {
    private ClassFinder classFinder;


    public void setUp() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        UrlSet urlSet = new UrlSet(classLoader);

        if (classLoader.getParent() != null){
            urlSet = urlSet.exclude(classLoader.getParent());
        }

        urlSet = urlSet.excludeJavaHome();

        classFinder = new ClassFinder(classLoader, urlSet.getUrls());
    }

    public void testFindAnnotatedPackages() throws Exception {
        List<Package> packages = classFinder.findAnnotatedPackages(Deployable.class);

        assertNotNull(packages);
        assertEquals(1, packages.size());
        assertTrue(packages.contains(Red.class.getPackage()));
    }

    public void testFindAnnotatedClasses() throws Exception {

        Class[] expected = {Halloween.class, Thanksgiving.class, ValentinesDay.class, GenericHoliday.class};
        List<Class<?>> actual = classFinder.findAnnotatedClasses(Holiday.class);

        assertNotNull(actual);
        assertEquals(expected.length, actual.size());
        for (Class clazz : expected) {
            assertTrue(clazz.getName(), actual.contains(clazz));
        }

        Class[] expected2 = {Blue.class, Blue.Navy.class, Blue.Sky.class, Green.class, Green.Emerald.class, Red.class, Red.CandyApple.class, Red.Pink.class};
        actual = classFinder.findAnnotatedClasses(Color.class);

        assertNotNull(actual);
        assertEquals(expected2.length, actual.size());
        for (Class clazz : expected2) {
            assertTrue(clazz.getName(), actual.contains(clazz));
        }

        Class[] expected3 = {Type.class};
        actual = classFinder.findAnnotatedClasses(AnnType.class);

        assertNotNull(actual);
        assertEquals(expected3.length, actual.size());
        for (Class clazz : expected3) {
            assertTrue(clazz.getName(), actual.contains(clazz));
        }
    }

    public void testFindInheritedAnnotatedClassesInherited() throws Exception {
        Class[] expected = {FunnyFamilyHalloween.class, FamilyHalloween.class, Halloween.class, Thanksgiving.class, ValentinesDay.class, GenericHoliday.class, StringGenericHoliday.class};
        List<Class<?>> actual = classFinder.findInheritedAnnotatedClasses(Holiday.class);

        assertNotNull(actual);
        assertEquals(expected.length, actual.size());
        for (Class clazz : expected) {
            assertTrue(clazz.getName(), actual.contains(clazz));
        }

        expected = new Class[]{Halloween.class, Thanksgiving.class, ValentinesDay.class, GenericHoliday.class};
        actual = classFinder.findAnnotatedClasses(Holiday.class);
        assertNotNull(actual);
        assertEquals(expected.length, actual.size());
        for (Class clazz : expected) {
            assertTrue(clazz.getName(), actual.contains(clazz));
        }
    }

    public void testFindAnnotatedMethods() throws Exception {
        List<Method> methods = classFinder.findAnnotatedMethods(Get.class);
        assertNotNull("methods", methods);
        assertEquals("methods.size", 5, methods.size());

        // Annotated parameters don't count
        methods = classFinder.findAnnotatedMethods(ParamA.class);
        assertNotNull("methods", methods);
        assertEquals("methods.size", 0, methods.size());

        // Neither do annotated constructors
        methods = classFinder.findAnnotatedMethods(Construct.class);
        assertNotNull("methods", methods);
        assertEquals("methods.size", 0, methods.size());
    }

    public void testFindAnnotatedConstructors() throws Exception {
        List<Constructor> constructors = classFinder.findAnnotatedConstructors(Construct.class);
        assertNotNull("constructors", constructors);
        assertEquals("constructors.size", 1, constructors.size());
    }

    public void testFindAnnotatedFields() throws Exception {
        List<Field> fields = classFinder.findAnnotatedFields(org.acme.bar.Field.class);
        assertNotNull("fields", fields);
        assertEquals("fields.size", 7, fields.size());
    }

    public void testClassListConstructor() throws Exception {
        Class[] classes = {Blue.class, Blue.Navy.class, Blue.Sky.class, Green.class, Green.Emerald.class, Red.class,
                Red.CandyApple.class, Red.Pink.class, Halloween.class, Holiday.class, Deployable.class, Primary.class,
                Property.class, Thanksgiving.class, ValentinesDay.class, FullyAnnotated.class, Type.class,
                GenericHoliday.class, StringGenericHoliday.class};

        classFinder = new ClassFinder(classes);

        testFindAnnotatedClasses();
        testFindAnnotatedConstructors();
        testFindAnnotatedFields();
        testFindAnnotatedMethods();
        testFindAnnotatedPackages();
    }
    public void testFindClassesInPackage() throws Exception{
    	List<Class<?>> classesInPackage = classFinder.findClassesInPackage("org.acme.foo", false);
    	Class<?>[] classesArray = {Blue.class, Blue.Navy.class, Blue.Sky.class, Green.class, Green.Emerald.class, Red.class,
                Red.CandyApple.class, Red.Pink.class, Halloween.class, Holiday.class, Deployable.class, Primary.class,
                Property.class, Thanksgiving.class, ValentinesDay.class};
    	List<Class<?>> classes = Arrays.asList(classesArray);
    	assertEquals(true, classesInPackage.containsAll(classes));
    }
}
