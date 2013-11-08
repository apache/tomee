/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REUtil;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryImpl;
import org.apache.openjpa.lib.util.Localizer;

/**
 * TestCase framework to run various tests against solarmetric code.
 * This class contains various utility methods for the following functions:
 * <ul>
 * <li>Using multiple, isolated ClassLoaders</li>
 * <li>Running a test in multiple concurrent threads</li>
 * <li>Assertion helpers</li>
 * <li>Creating random Strings, numbers, etc.</li>
 * </ul>
 *
 * @author Marc Prud'hommeaux
 * @author Patrick Linskey
 */
public abstract class AbstractTestCase extends TestCase {

    public static final String TEST_METHODS =
        System.getProperty(AbstractTestCase.class.getName() + ".testMethods");
    public static final long PLATFORM_ALL = 2 << 1;
    public static final long PLATFORM_UNKNOWN = 2 << 2;

    public static final String SKIP_TOKEN = "SOLARSKIP";
    public static final String SKIP_DELIMITER = "|";

    private static final Localizer _loc =
        Localizer.forPackage(AbstractTestCase.class);

    private Log log = null;

    private static Map _times = new HashMap();

    private static AbstractTestCase _lastTest = null;

    private static WatchdogThread _watchdog = new WatchdogThread();
    private long _timeout;

    /**
     * Constructor. Create a test case with the specified name.
     */
    public AbstractTestCase(String test) {
        super(test);
    }

    public AbstractTestCase() {
    }

    protected final Log getLog() {
        if (log == null)
            log = newLog();
        return log;
    }

    protected Log newLog() {
        // this implementation leaves much to be desired, as it just
        // creates a new LogFactoryImpl each time, and does not apply
        // any configurations.
        return new LogFactoryImpl().getLog(getLogName());
    }

    protected String getLogName() {
        return "com.solarmetric.Runtime";
    }

    /**
     * Called before the watchdog thread is about to kill the entire
     * JVM due to a test case's timeout. This method offers the
     * ability to try to resolve whatever contention is taking place
     * in the test. It will be given 10 seconds to try to end the
     * test peacefully before the watchdog exits the JVM.
     */
    protected void preTimeout() {
    }

    public void run(TestResult result) {
        if (skipTest()) {
            // keep track of the tests we skip so that we can get an
            // idea in the autobuild status
            System.err.println(SKIP_TOKEN + SKIP_DELIMITER
                + ("" + getClass().getName())
                + "." + getName() + SKIP_DELIMITER);
            return;
        }

        if (_lastTest != null && _lastTest.getClass() != getClass()) {
            try {
                _lastTest.tearDownTestClass();
            } catch (Throwable t) {
                getLog().error(null, t);
            }
        }

        if (_lastTest == null || _lastTest.getClass() != getClass()) {
            try {
                setUpTestClass();
            } catch (Throwable t) {
                getLog().error(null, t);
            }
        }

        _lastTest = this;

        // inform the watchdog thread that we are entering the test
        _watchdog.enteringTest(this);
        try {
            super.run(result);
        } finally {
            _watchdog.leavingTest(this);
        }
    }

    /**
     * If this test should be skipped given the current
     * environment, return <code>true</code>. This allows a unit test
     * class to disable test cases on a per-method granularity, and
     * prevents the test from showing up as a passed test just
     * because it was skipped.
     * For example, if a particular test case method should not be
     * run against a certain database, this method could check the
     * name of the test result and the current database configuration
     * in order to make the decision:
     * <p/>
     * <code> protected boolean skipTest() {
     * // don't run with pointbase: it uses a DataSource, which
     * // can't be translated into a JBoss DataSource configuration.
     * if ("testJBoss".equals(getName()) &&
     * getCurrentPlatform() == PLATFORM_POINTBASE)
     * return true;
     * }
     * </code>
     * If you want to disable execution of an entire test case
     * class for a given database, you might want to add the class to
     * the excluded test list in that database's properties file.
     */
    protected boolean skipTest() {
        if (TEST_METHODS != null && TEST_METHODS.length() > 0)
            return TEST_METHODS.indexOf(getName()) == -1;

        return false;
    }

    /**
     * This method is called before the first test in this test class
     * is executed.
     */
    public void setUpTestClass() throws Exception {
    }

    /**
     * This method is called after the last test in this test class
     * is executed. It can be used to do things like clean up
     * large, slow processes that may have been started.
     */
    public void tearDownTestClass() throws Exception {
    }

    public void tearDown() throws Exception {
        if ("true".equals(System.getProperty("meminfo")))
            printMemoryInfo();

        super.tearDown();
    }

    //////////////////////////
    // Generating random data
    //////////////////////////

    /**
     * Support method to get a random Integer for testing.
     */
    public static Integer randomInt() {
        return new Integer((int) (Math.random() * Integer.MAX_VALUE));
    }

    /**
     * Support method to get a random Character for testing.
     */
    public static Character randomChar() {
        char [] TEST_CHAR_ARRAY = new char []{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1',
            '2', '3', '4', '5', '6', '7', '8', '9' };

        return new Character(TEST_CHAR_ARRAY[
            (int) (Math.random() * TEST_CHAR_ARRAY.length)]);
    }

    /**
     * Support method to get a random Long for testing.
     */
    public static Long randomLong() {
        return new Long((long) (Math.random() * Long.MAX_VALUE));
    }

    /**
     * Support method to get a random Short for testing.
     */
    public static Short randomShort() {
        return new Short((short) (Math.random() * Short.MAX_VALUE));
    }

    /**
     * Support method to get a random Double for testing.
     */
    public static Double randomDouble() {
        return new Double((double) (Math.round(Math.random() * 5000d)) / 1000d);
    }

    /**
     * Support method to get a random Float for testing.
     */
    public static Float randomFloat() {
        return new Float((float) (Math.round(Math.random() * 5000f)) / 1000f);
    }

    /**
     * Support method to get a random Byte for testing.
     */
    public static Byte randomByte() {
        return new Byte((byte) (Math.random() * Byte.MAX_VALUE));
    }

    /**
     * Support method to get a random Boolean for testing.
     */
    public static Boolean randomBoolean() {
        return new Boolean(Math.random() > 0.5 ? true : false);
    }

    /**
     * Support method to get a random Date for testing.
     */
    public static Date randomDate() {
        long millis = (long) (Math.random() * System.currentTimeMillis());

        // round millis to the nearest 1000: this is because some
        // databases do not store the milliseconds correctly(e.g., MySQL).
        // This is a really a bug we should fix. FC #27.
        millis -= (millis % 1000);

        return new Date(millis);
    }

    /**
     * Support method to get a random String for testing.
     */
    public static String randomString() {
        // default to a small string, in case column sizes are
        // limited(such as with a string primary key)
        return randomString(50);
    }

    /**
     * Support method to get a random String for testing.
     */
    public static String randomString(int len) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < (int) (Math.random() * len) + 1; i++)
            buf.append(randomChar());
        return buf.toString();
    }

    /**
     * Support method to get a random clob for testing.
     */
    public static String randomClob() {
        StringBuffer sbuf = new StringBuffer();
        while (sbuf.length() < (5 * 1024)) { // at least 5K
            sbuf.append(randomString(1024));
        }

        return sbuf.toString();
    }

    /**
     * Support method to get a random BigInteger for testing.
     */
    public static BigInteger randomBigInteger() {
        // too many of our test databases don't support bigints > MAX_LONG:
        // I don't like it, but for now, let's only test below MAX_LONG
        BigInteger lng = new BigInteger(
            ((long) (Math.random() * Long.MAX_VALUE)) + "");

        BigInteger multiplier = new BigInteger("1");
        // (1 + (int)(Math.random() * 10000)) + "");
        if (Math.random() < 0.5)
            multiplier = multiplier.multiply(new BigInteger("-1"));

        return lng.multiply(multiplier);
    }

    /**
     * Support method to get a random BigDecimal for testing.
     */
    public static BigDecimal randomBigDecimal() {
        BigInteger start = randomBigInteger();
        String str = start.toString();
        // truncate off the last 8 digits: we still get some
        // overflows with lame databases.
        for (int i = 0; i < 8; i++)
            if (str.length() > 2)
                str = str.substring(0, str.length() - 1);
        start = new BigInteger(str);

        String val = start + "." + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10))
            + ((int) (Math.random() * 10));

        return new BigDecimal(val);
    }

    /**
     * Support method to get a random blob for testing.
     */
    public static byte[] randomBlob() {
        // up to 100K blob
        byte [] blob = new byte [(int) (Math.random() * 1024 * 100)];
        for (int i = 0; i < blob.length; i++)
            blob[i] = randomByte().byteValue();

        return blob;
    }

    /**
     * Invoke setters for pimitives and primitive wrappers on the
     * specified object.
     */
    public static Object randomizeBean(Object bean)
        throws IntrospectionException, IllegalAccessException,
        InvocationTargetException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor [] props = info.getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
            Method write = props[i].getWriteMethod();
            if (write == null)
                continue;

            Class [] params = write.getParameterTypes();
            if (params == null || params.length != 1)
                continue;

            Class paramType = params[0];
            Object arg = null;

            if (paramType == boolean.class || paramType == Boolean.class)
                arg = randomBoolean();
            else if (paramType == byte.class || paramType == Byte.class)
                arg = randomByte();
            else if (paramType == char.class || paramType == Character.class)
                arg = randomChar();
            else if (paramType == short.class || paramType == Short.class)
                arg = randomShort();
            else if (paramType == int.class || paramType == Integer.class)
                arg = randomInt();
            else if (paramType == long.class || paramType == Long.class)
                arg = randomLong();
            else if (paramType == double.class || paramType == Double.class)
                arg = randomDouble();
            else if (paramType == float.class || paramType == Float.class)
                arg = randomFloat();
            else if (paramType == String.class)
                arg = randomString();
            else if (paramType == BigInteger.class)
                arg = randomBigInteger();
            else if (paramType == BigDecimal.class)
                arg = randomBigDecimal();
            else if (paramType == Date.class)
                arg = randomDate();

            if (arg != null)
                write.invoke(bean, new Object []{ arg });
        }

        return bean;
    }

    /**
     * Utility method to start a profile.
     *
     * @see #endProfile(String)
     */
    public void startProfile(String name) {
        _times.put(name, new Long(System.currentTimeMillis()));
    }

    /**
     * Utility to end the profile and print out the time. Example usage:
     * <p/>
     * <pre><code> startProfile("Some long task"); doSomeLongTask();
     * endProfile("Some long task");
     * </code></pre>
     *
     * @param name
     * @return the amount of time that this profile invocation took, or
     *         -1 if <code>name</code> was never started.
     */
    public long endProfile(String name) {
        Long time = (Long) _times.remove(name);

        long elapsed = -1;
        if (time != null)
            elapsed = System.currentTimeMillis() - time.longValue();

        getLog().info(_loc.get("profile-info", name,
            (time == null ? "???" : "" + elapsed)));
        return elapsed;
    }

    /////////////////////////
    // ClassLoader functions
    /////////////////////////

    /**
     * Create a ClassLoader that will not use the parent
     * ClassLoader to resolve classes. This is useful for
     * testing interactions between Kodo in running
     * in ClassLoaderA and instances in ClassLoaderB.
     */
    public ClassLoader createIsolatedClassLoader() {
        return new IsolatedClassLoader();
    }

    public NestedClassLoader createNestedClassLoader() {
        return new NestedClassLoader(false);
    }

    public NestedClassLoader createNestedParentClassLoader() {
        return new NestedClassLoader(true);
    }

    /**
     * Reload the specified class in an isolated ClassLoader.
     *
     * @param target the target class to load
     * @return the Class as reloaded in an new ClassLoader
     */
    public Class isolate(Class target) throws ClassNotFoundException {
        Class result = isolate(target.getName());
        assertTrue(result != target);
        assertNotEquals(result, target);
        assertTrue(result.getClassLoader() != target.getClassLoader());
        return result;
    }

    public Class isolate(String target) throws ClassNotFoundException {
        ClassLoader il = createIsolatedClassLoader();
        Class result = il.loadClass(target);
        assertEquals(result.getName(), target);

        return result;
    }

    public Class nest(Class target) throws ClassNotFoundException {
        ClassLoader il = createNestedClassLoader();
        Class result = il.loadClass(target.getName());
        assertTrue(result != target);
        assertNotEquals(result, target);
        assertTrue(result.getClassLoader() != target.getClassLoader());
        assertEquals(result.getName(), target.getName());

        return result;
    }

    public Object isolateNew(Class target)
        throws ClassNotFoundException, IllegalAccessException,
        InstantiationException {
        return isolate(target).newInstance();
    }

    private static class NestedClassLoader extends AntClassLoader {

        public NestedClassLoader(boolean useParent) {
            super(ClassLoader.getSystemClassLoader(), useParent);

            for (StringTokenizer cltok = new StringTokenizer(
                System.getProperty("java.class.path"), File.pathSeparator);
                cltok.hasMoreTokens();) {
                String path = cltok.nextToken();

                // only load test paths, not jar files
                if (path.indexOf(".jar") != -1)
                    continue;
                if (path.indexOf(".zip") != -1)
                    continue;

                addPathElement(path);
            }

            try {
                if (!useParent) {
                    assertTrue(loadClass
                        (AbstractTestCase.class.getName()).getClassLoader()
                        != AbstractTestCase.class.getClassLoader());
                }
            } catch (ClassNotFoundException cnfe) {
                fail(cnfe.toString());
            }
        }

        public Class findClass(String name) throws ClassNotFoundException {
            // don't isolate PC and related classes in kodo.enhnace
            if (name.indexOf(".enhance.") != -1)
                throw new ClassNotFoundException(name);
            if (name.indexOf("/enhance/") != -1)
                throw new ClassNotFoundException(name);
            return super.findClass(name);
        }
    }

    /**
     * A ClassLoader that is completely isolated with respect to
     * any classes that are loaded in the System ClassLoader.
     *
     * @author Marc Prud'hommeaux
     */
    private static class IsolatedClassLoader extends NestedClassLoader {

        public IsolatedClassLoader() {
            super(false);
            setIsolated(false);
        }
    }

    ///////////////
    // Collections
    ///////////////

    /**
     * Validate that the specified {@link Collection} fulfills the
     * Collection contract as specified by the Collections API.
     * <p/>
     * <strong>Note</strong>: does not validate mutable operations
     */
    public static void validateCollection(Collection collection) {
        int size = collection.size();
        int iterated = 0;
        // ensure we can walk along the iterator
        for (Iterator i = collection.iterator(); i.hasNext();) {
            iterated++;
            i.next();
        }

        // ensure the number of values iterated is the same as the list size
        assertEquals(size, iterated);

        // also validate the list
        if (collection instanceof List) {
            List ll = new ArrayList();
            for (int i = 0; i < 100; i++)
                ll.add(new Integer(i));
            validateList((List) ll);
            validateList((List) collection);
        }
    }

    /**
     * Validate that the specified {@link List} fulfills the
     * List contract as specified by the Collections API.
     * <p/>
     * <strong>Note</strong>: does not validate mutable operations
     */
    public static void validateList(List list) {
        Object [] coreValues = list.toArray();
        Object [] values1 = new Object [list.size()];
        Object [] values2 = new Object [list.size()];
        Object [] values3 = new Object [list.size()];
        Object [] values4 = new Object [list.size()];

        // fill sequential index access list
        for (int i = 0; i < list.size(); i++)
            values1[i] = list.get(i);

        // fill sequential list
        int index = 0;
        ListIterator iter;
        for (iter = list.listIterator(0); iter.hasNext();) {
            assertEquals(index, iter.nextIndex());
            assertEquals(index, iter.previousIndex() + 1);
            values2[index] = iter.next();
            assertTrue(list.contains(values2[index]));
            index++;
        }

        // ensure NoSuchElementException is thrown as appropriate
        try {
            iter.next();
            fail("next() should have resulted in a NoSuchElementException");
        } catch (NoSuchElementException e) {
        } // as expected

        // fill reverse sequential list
        int back = 0;
        for (iter = list.listIterator(list.size()); iter.hasPrevious();) {
            assertEquals(index, iter.previousIndex() + 1);
            assertEquals(index, iter.nextIndex());
            values3[--index] = iter.previous();
            back++;
        }
        assertEquals(list.size(), back);

        // ensure NoSuchElementException is thrown as appropriate
        try {
            iter.previous();
            fail("previous() should have resulted in a "
                + "NoSuchElementException");
        } catch (NoSuchElementException e) {
        } // as expected

        // fill random access list
        List indices = new LinkedList();
        for (int i = 0; i < list.size(); i++)
            indices.add(new Integer(i));

        for (int i = 0; i < list.size(); i++) {
            int rand = (int) (Math.random() * indices.size());
            Integer randIndex = (Integer) indices.remove(rand);
            values4[randIndex.intValue()] = list.get(randIndex.intValue());
        }

        assertEquals(Arrays.asList(coreValues), Arrays.asList(values1));
        assertIdentical(Arrays.asList(coreValues), Arrays.asList(values1));
        assertEquals(Arrays.asList(coreValues), Arrays.asList(values2));
        assertIdentical(Arrays.asList(coreValues), Arrays.asList(values2));
        assertEquals(Arrays.asList(coreValues), Arrays.asList(values4));
        assertIdentical(Arrays.asList(coreValues), Arrays.asList(values4));
        assertEquals(Arrays.asList(coreValues), Arrays.asList(values3));
        assertIdentical(Arrays.asList(coreValues), Arrays.asList(values3));
    }

    /**
     * Assert that the given List contain the exact same
     * elements. This is different than the normal List contract, which
     * states that list1.equals(list2) if each element e1.equals(e2).
     * This method asserts that e1 == n2.
     */
    public static void assertIdentical(List c1, List c2) {
        assertEquals(c1.size(), c2.size());
        for (Iterator i1 = c1.iterator(), i2 = c2.iterator();
            i1.hasNext() && i2.hasNext();)
            assertTrue(i1.next() == i2.next());
    }

    /**
     * Assert that the collection parameter is already ordered
     * according to the specified comparator.
     */
    public void assertOrdered(Collection c, Comparator comp) {
        List l1 = new LinkedList(c);
        List l2 = new LinkedList(c);
        assertEquals(l1, l2);
        Collections.sort(l2, comp);
        assertEquals(l1, l2);
        Collections.sort(l1, comp);
        assertEquals(l1, l2);
    }

    ////////////////////
    // Assertion Helpers
    ////////////////////

    public void assertNotEquals(Object a, Object b) {
        if (a == null && b != null)
            return;
        if (a != null && b == null)
            return;
        if (!(a.equals(b)))
            return;
        if (!(b.equals(a)))
            return;

        fail("expected !<" + a + ">.equals(<" + b + ">)");
    }

    public void assertSize(int size, Object ob) {
        if (ob == null) {
            assertEquals(size, 0);
            return;
        }

        if (ob instanceof Collection)
            ob = ((Collection) ob).iterator();
        if (ob instanceof Iterator) {
            Iterator i = (Iterator) ob;
            int count = 0;
            while (i.hasNext()) {
                count++;
                i.next();
            }

            assertEquals(size, count);
        } else
            fail("assertSize: expected Collection, Iterator, "
                + "Query, or Extent, but got " + ob.getClass().getName());
    }

    /////////////////////
    // Generic utilities
    /////////////////////

    public void copy(File from, File to) throws IOException {
        copy(new FileInputStream(from), to);
    }

    public void copy(InputStream in, File to) throws IOException {
        FileOutputStream fout = new FileOutputStream(to);

        byte[] b = new byte[1024];

        for (int n = 0; (n = in.read(b)) != -1;)
            fout.write(b, 0, n);
    }

    /**
     * Print out information on memory usage.
     */
    public void printMemoryInfo() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;

        NumberFormat nf = NumberFormat.getInstance();
        getLog().warn(_loc.get("mem-info",
            nf.format(used),
            nf.format(total),
            nf.format(free)));
    }

    /**
     * Return a list of all values iterated by the given iterator.
     */
    public static List iteratorToList(Iterator i) {
        LinkedList list = new LinkedList();
        while (i.hasNext())
            list.add(i.next());
        return list;
    }

    /**
     * Return an array of the objects iterated by the given iterator.
     */
    public static Object [] iteratorToArray(Iterator i, Class [] clazz) {
        return iteratorToList(i).toArray(clazz);
    }

    /**
     * Run ant on the specified build file.
     *
     * @param buildFile the build file to use
     * @param target the name of the target to invoke
     */
    public void ant(File buildFile, String target) {
        assertTrue(buildFile.isFile());

        Project project = new Project();
        project.init();
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        ProjectHelper.configureProject(project, buildFile);
        project.executeTarget(target);
    }

    /**
     * Serialize and deserialize the object.
     *
     * @param validateEquality make sure the hashCode and equals
     * methods hold true
     */
    public static Object roundtrip(Object orig, boolean validateEquality)
        throws IOException, ClassNotFoundException {
        assertNotNull(orig);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(orig);
        ByteArrayInputStream bin = new ByteArrayInputStream(
            bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        Object result = in.readObject();

        if (validateEquality) {
            assertEquals(orig, result);
            assertEquals(orig.hashCode(), result.hashCode());
        }

        return result;
    }

    /**
     * @return true if the specified input matches the regular expression regex.
     */
    public static boolean matches(String regex, String input)
        throws RESyntaxException {
        RE re = REUtil.createRE(regex);
        return re.match(input);
    }

    public static void assertMatches(String regex, String input) {
        try {
            if (!(matches(regex, input)))
                fail("Expected regular expression: <" + regex + ">"
                    + " did not match: <" + input + ">");
        } catch (RESyntaxException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public static void assertNotMatches(String regex, String input) {
        try {
            if (matches(regex, input))
                fail("Regular expression: <" + regex + ">"
                    + " should not match: <" + input + ">");
        } catch (RESyntaxException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Check the list if strings and return the ones that match
     * the specified match.
     */
    public static List matches(String regex, Collection input)
        throws RESyntaxException {
        List matches = new ArrayList();
        for (Iterator i = input.iterator(); i.hasNext();) {
            String check = (String) i.next();
            if (matches(regex, check))
                matches.add(check);
        }

        return matches;
    }

    /**
     * Assert that the specified collection of Strings contains at least
     * one string that matches the specified regular expression.
     */
    public static void assertMatches(String regex, Collection input) {
        try {
            if (matches(regex, input).size() == 0)
                fail("The specified list of size " + input.size()
                    + " did not contain any strings that match the"
                    + " specified regular expression(\"" + regex + "\")");
        } catch (RESyntaxException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Assert that the specified collection of Strings does not match
     * the specified regular expression.
     */
    public static void assertNotMatches(String regex, Collection input) {
        try {
            List matches;

            if (((matches = matches(regex, input))).size() > 0)
                fail("The specified list of size " + input.size()
                    + " did contain one or more strings that matchs the"
                    + " specified illegal regular expression"
                    + " (\"" + regex + "\")."
                    + " First example of a matching message is: "
                    + matches.iterator().next());
        } catch (RESyntaxException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * To be called by the child. E.g.:
     * <code> public static void main(String [] args) { main(TestBug375.class);
     * }
     * </code>
     */
    public static void main(Class c) {
        TestRunner.run(c);
    }

    /**
     * To be called by child. Figures out the class from the calling context.
     */
    public static void main() {
        String caller = new SecurityManager() {
            public String toString() {
                return getClassContext()[2].getName();
            }
        }.toString();

        try {
            main(Class.forName(caller));
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe.toString());
        }
    }

    /**
     * Returns the jar file in which the class is contained.
     *
     * @return the jar file, or none if the class is not in a jar
     * @throws FileNotFoundException if the jar file cannot located
     */
    public static File getJarFile(Class clazz) throws FileNotFoundException {
        URL url = clazz.getResource(clazz.getName().substring(
            clazz.getName().lastIndexOf(".") + 1) + ".class");
        if (url == null)
            throw new FileNotFoundException(clazz.toString());

        String file = url.getFile();
        if (file == null)
            throw new FileNotFoundException(url.toString());
        int index = file.indexOf("!");
        if (index == -1)
            throw new FileNotFoundException(file);

        file = file.substring(0, index);
        file = file.substring("file:".length());

        File f = new File(file);
        if (!(f.isFile()))
            throw new FileNotFoundException(file);

        return f.getAbsoluteFile();
    }

    /**
     * The number of milliseconds each test case will have for a timeout.
     */
    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    /**
     * The number of milliseconds each test case will have for a timeout.
     */
    public long getTimeout() {
        return _timeout;
    }

    /**
     * A watchdog that just exits the JVM if a test has not completed in
     * a certain amount of time. This speeds up the mechanism of determining
     * if a timeout has occurred, since we can exit the entire test run
     * if a test hasn't completed in a shorted amount of time than
     * the global test timeout.
     *
     * @author Marc Prud'hommeaux
     */
    private static class WatchdogThread extends Thread {

        private final long _timeoutms;
        private long _endtime = -1;
        private AbstractTestCase _curtest = null;

        public WatchdogThread() {
            super("Kodo test case watchdog thread");
            setDaemon(true);

            int timeoutMin = new Integer
                (System.getProperty("autobuild.testcase.timeout", "20"))
                .intValue();

            _timeoutms = timeoutMin * 60 * 1000;
        }

        public void run() {
            while (true) {
                try {
                    sleep(200);
                } catch (InterruptedException ie) {
                }

                if (_endtime > 0 && System.currentTimeMillis() > _endtime) {
                    Thread preTimeout = new Thread
                        ("Attempting pre-timeout for " + _curtest) {
                        public void run() {
                            _curtest.preTimeout();
                        }
                    };
                    preTimeout.start();

                    // wait a little while for the pre-timeout
                    // thread to complete
                    try {
                        preTimeout.join(10 * 1000);
                    } catch (Exception e) {
                    }

                    // give it a few more seconds...
                    try {
                        sleep(5 * 1000);
                    } catch (Exception e) {
                    }

                    // new endtime? resume...
                    if (System.currentTimeMillis() < _endtime)
                        continue;

                    new Exception("test case "
                        + (_curtest != null ? _curtest.getName()
                        : "UNKNOWN") + " timed out after "
                        + _timeoutms + "ms").printStackTrace();

                    // also run "killall -QUIT java" to try to grab
                    // a stack trace
                    try {
                        Runtime.getRuntime().exec
                            (new String[]{ "killall", "-QUIT", "java" });
                    } catch (Exception e) {
                    }

                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                    }

                    // now actually exit
                    System.exit(111);
                }
            }
        }

        public synchronized void enteringTest(AbstractTestCase test) {
            long timeout = test.getTimeout();
            if (timeout <= 0)
                timeout = _timeoutms;

            _endtime = System.currentTimeMillis() + timeout;
            _curtest = test;

            if (!isAlive())
                start();
        }

        public synchronized void leavingTest(AbstractTestCase test) {
            _endtime = -1;
            _curtest = null;
        }
    }
}
