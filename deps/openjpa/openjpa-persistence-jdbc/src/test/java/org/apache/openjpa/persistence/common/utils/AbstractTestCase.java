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
package org.apache.openjpa.persistence.common.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.management.IntrospectionException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REUtil;

/**
 * Extends AbstractCachedEMFTestCase, which extends AbstractPersistenceTestCase.
 * Cleans up EMFs returned by getEmf() in tearDown().
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractTestCase extends AbstractCachedEMFTestCase {

    private String persistenceXmlResource;
    private Map<Map,OpenJPAEntityManagerFactory> emfs =
        new HashMap<Map,OpenJPAEntityManagerFactory>();
    private OpenJPAEntityManager currentEntityManager;
    private Object[] props;

    protected enum Platform {
        EMPRESS,
        HYPERSONIC,
        POSTGRESQL,
        MARIADB,
        MYSQL,
        SQLSERVER,
        DB2,
        ORACLE,
        DERBY,
        INFORMIX,
        POINTBASE,
        SYBASE,
        INGRES,
    }

    protected String multiThreadExecuting = null;
    protected boolean inTimeoutThread = false;


    public AbstractTestCase(String name, String s) {
        setName(name);
        persistenceXmlResource = computePersistenceXmlResource(s);
    }

    /**
     * Use the given persistent types during the test.
     * 
     * @param props
     *            list of persistent types used in testing and/or configuration values in the form
     *            key,value,key,value...
     */
    protected void setUp(Object... props) throws Exception {
        super.setUp();
        this.props = props;
    }

    /**
     * Closes any EMFs created by getEmf()
     * 
     */
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            for (EntityManagerFactory emf : emfs.values()) {
                try {
                    // closeEMF() will also close any open/active EMs
                    closeEMF(emf);
                    emf = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AbstractTestCase() {
    }

    public AbstractTestCase(String name) {
        setName(name);
    }

    protected String computePersistenceXmlResource(String s) {
        String resourceName = getClass().getPackage().getName()
            .replaceAll("\\.", "/");
        resourceName += "/common/apps/META-INF/persistence.xml";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource != null)
            return resourceName;
        return defaultPersistenceXmlResource();
    }

    protected String defaultPersistenceXmlResource() {
        return "org/apache/openjpa/persistence/" +
            "common/apps/META-INF/persistence.xml";
    }

    protected OpenJPAStateManager getStateManager(Object obj,
        EntityManager em) {
        return JPAFacadeHelper.toBroker(em).getStateManager(obj);
    }

    protected int deleteAll(Class type, EntityManager em) {
        ClassMetaData meta = JPAFacadeHelper.getMetaData(em, type);
        if (meta != null)
            return em.createQuery("delete from " + meta.getTypeAlias())
                .executeUpdate();
        else
            return -1;
    }

    protected int deleteAll(Class... types) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();
        int ret = 0;
        for (Class type : types)
            ret += deleteAll(type, em);
        em.getTransaction().commit();
        em.close();
        return ret;
    }

    /**
     * Creates a EMF and adds it to a Map for cleanup in tearDown()
     * 
     * @param map
     * @return
     */
    protected OpenJPAEntityManagerFactory getEmf(Map map) {
        if (map == null)
            map = new HashMap();
        Collection keys = new ArrayList();
        for (Object key : map.keySet())
            if (key.toString().startsWith("kodo"))
                keys.add(key);
        if (keys.size() > 0)
            throw new IllegalArgumentException(
                "kodo-prefixed properties must be converted to openjpa. " +
                    "Properties: " + keys);

        addProperties(map);

        OpenJPAEntityManagerFactory emf = emfs.get(map);
        if (emf != null) {
            return emf;
        }

        if (props != null) {
            // Join properties passed in setUp (usually entities) with the given map and use them to create EMF.
            Object[] propsAndMap = new Object[props.length + map.size() * 2];
            System.arraycopy(props, 0, propsAndMap, 0, props.length);
            int i = props.length;
            for (Object o : map.entrySet()) {
                Entry mapEntry = (Entry) o;
                propsAndMap[i++] = mapEntry.getKey();
                propsAndMap[i++] = mapEntry.getValue();
            }
            emf = createEMF(propsAndMap);
        } else {
            emf = OpenJPAPersistence.createEntityManagerFactory("TestConv", persistenceXmlResource, map);
        }
        emfs.put(map, emf);
        return emf;
    }

    protected void addProperties(Map map) {
        if (!map.containsKey("openjpa.jdbc.SynchronizeMappings"))
            map.put("openjpa.jdbc.SynchronizeMappings",
                "buildSchema(ForeignKeys=true," +
                    "SchemaAction='add,deleteTableContents')");
    }

    /**
     * Creates a EMF and adds it to a Map for cleanup in tearDown()
     * 
     * @return
     */
    protected OpenJPAEntityManagerFactory getEmf() {
        Map m = new HashMap();
        return getEmf(m);
    }

    protected BrokerFactory getBrokerFactory() {
        return JPAFacadeHelper.toBrokerFactory(getEmf());
    }

    protected BrokerFactory getBrokerFactory(String[] args) {
        if (args.length % 2 != 0)
            throw new IllegalArgumentException(
                "odd number of elements in arg array");
        Map map = new HashMap();
        for (int i = 0; i < args.length; i = i + 2)
            map.put(args[i], args[i+1]);
        return JPAFacadeHelper.toBrokerFactory(getEmf(map));
    }

    protected OpenJPAEntityManager currentEntityManager() {
        if (currentEntityManager == null || !currentEntityManager.isOpen())
            currentEntityManager = getEmf().createEntityManager();
        return currentEntityManager;
    }

    protected void startTx(EntityManager em) {
        em.getTransaction().begin();
    }

    protected boolean isActiveTx(EntityManager em) {
        return em.getTransaction().isActive();
    }

    protected void endTx(EntityManager em) {
        if (em.getTransaction().isActive()) {
            if (em.getTransaction().getRollbackOnly())
                em.getTransaction().rollback();
            else
                em.getTransaction().commit();
        }
    }

    protected void rollbackTx(EntityManager em) {
        em.getTransaction().rollback();
    }

    protected void endEm(EntityManager em) {
        if (em != null && em.isOpen())
            em.close();
        if (em == currentEntityManager)
            currentEntityManager = null;
    }

    protected Object getStackTrace(Throwable t) {
        throw new UnsupportedOperationException();
    }

    protected OpenJPAConfiguration getConfiguration() {
        return getEmf().getConfiguration();
    }

    protected Platform getCurrentPlatform() {
        throw new UnsupportedOperationException();
    }

    protected void bug(int id, String s) {
        bug(id, null, s);
    }
    
    protected void bug(Platform platform, int id, Throwable t, String s) {
        bug(EnumSet.of(platform), id, t, s);
    }

    protected void bug(EnumSet<Platform> platforms, int id, Throwable t,
            String s) {
        if (platforms.contains(getCurrentPlatform()))
            bug(id, t, s);
        else
            fail(String.format(
                "bug %s is unexpectedly occurring on platform %s",
                id, getCurrentPlatform()));
    }

    protected void bug(int id, Throwable t, String s) {
        if (t != null) {
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            else
                throw new RuntimeException(t);
        } else {
            fail(s);
        }
    }

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
        // databases do not store the milliseconds correctly (e.g., MySQL).
        // This is a really a bug we should fix. FC #27.
        millis -= (millis % 1000);

        return new Date(millis);
    }

    /**
     * Support method to get a random String for testing.
     */
    public static String randomString() {
        // default to a small string, in case column sizes are
        // limited (such as with a string primary key)
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
        while (sbuf.length() < (5 * 1024)) // at least 5K
        {
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
        // (1 + (int)(Math.random () * 10000)) + "");
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

        String val = start + "."
                + ((int) (Math.random() * 10))
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
        InvocationTargetException, java.beans.IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] props = info.getPropertyDescriptors();
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

    protected void assertSize(int size, Collection c) {
        assertEquals(size, c.size());
    }

    protected void assertSize(int size, Query q) {
        assertEquals(size, q.getResultList().size());
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

        try {
            if (validateEquality) {
                assertEquals(orig.hashCode(), result.hashCode());
                assertEquals(orig, result);
            }
        } finally {
            out.close();
            in.close();
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

    protected Log getLog() {
        return getConfiguration().getLog("Tests");
    }

    ///////////////////
    // Multi threading
    ///////////////////

    /**
     * Re-execute the invoking method a random number of times
     * in a random number of Threads.
     */
    public void mttest() throws ThreadingException {
        // 6 iterations in 8 threads is a good trade-off between
        // tests taking way too long and having a decent chance of
        // identifying MT problems.
        int iterations = 6;
        int threads = 8;

        mttest(threads, iterations);
    }

    /**
     * Execute the calling method <code>iterations</code>
     * times in <code>threads</code> Threads.
     */
    public void mttest(int threads, int iterations) {
        mttest(0, threads, iterations);
    }

    public void mttest(int serialCount, int threads, int iterations)
        throws ThreadingException {
        String methodName = callingMethod("mttest");
        mttest(serialCount, threads, iterations, methodName, new Object [0]);
    }

    /**
     * Execute a test method in multiple threads.
     *
     * @param threads the number of Threads to run in
     * @param iterations the number of times the method should
     * be execute in a single Thread
     * @param method the name of the method to execute
     * @param args the arguments to pass to the method
     * @throws ThreadingException if an errors occur in
     * any of the Threads. The actual exceptions
     * will be embedded in the exception. Note that
     * this means that assert() failures will be
     * treated as errors rather than warnings.
     * @author Marc Prud'hommeaux
     */
    public void mttest(int threads, int iterations, final String method,
        final Object [] args) throws ThreadingException {
        mttest(0, threads, iterations, method, args);
    }

    public void mttest(int serialCount,
        int threads, int iterations, final String method, final Object [] args)
        throws ThreadingException {
        if (multiThreadExecuting != null && multiThreadExecuting.equals(method))
        {
            // we are currently executing in multi-threaded mode:
            // don't deadlock!
            return;
        }

        multiThreadExecuting = method;

        try {
            Class [] paramClasses = new Class [args.length];
            for (int i = 0; i < paramClasses.length; i++)
                paramClasses[i] = args[i].getClass();

            final Method meth;

            try {
                meth = getClass().getMethod(method, paramClasses);
            } catch (NoSuchMethodException nsme) {
                throw new ThreadingException(nsme.toString(), nsme);
            }

            final Object thiz = this;

            mttest("reflection invocation: (" + method + ")",
                serialCount, threads, iterations, new VolatileRunnable() {
                public void run() throws Exception {
                    meth.invoke(thiz, args);
                }
            });
        } finally {
            multiThreadExecuting = null;
        }
    }

    public void mttest(String title, final int threads, final int iterations,
        final VolatileRunnable runner) throws ThreadingException {
        mttest(title, 0, threads, iterations, runner);
    }

    /**
     * Execute a test method in multiple threads.
     *
     * @param title a description of the test, for inclusion in the
     * error message
     * @param serialCount the number of times to run the method
     * serially before spawning threads.
     * @param threads the number of Threads to run in
     * @param iterations the number of times the method should
     * @param runner the VolatileRunnable that will execute
     * the actual test from within the Thread.
     * @throws ThreadingException if an errors occur in
     * any of the Threads. The actual exceptions
     * will be embedded in the exception. Note that
     * this means that assert() failures will be
     * treated as errors rather than warnings.
     * @author Marc Prud'hommeaux be execute in a single Thread
     * @author Marc Prud'hommeaux
     */
    public void mttest(String title, final int serialCount,
        final int threads, final int iterations, final VolatileRunnable runner)
        throws ThreadingException {
        final List exceptions = Collections.synchronizedList(new LinkedList());

        Thread [] runners = new Thread [threads];

        final long startMillis = System.currentTimeMillis() + 1000;

        for (int i = 1; i <= threads; i++) {
            final int thisThread = i;

            runners[i - 1] =
                new Thread(title + " [" + i + " of " + threads + "]") {
                    public void run() {
                        // do our best to have all threads start at the exact
                        // same time. This is imperfect, but the closer we
                        // get to everyone starting at the same time, the
                        // better chance we have for identifying MT problems.
                        while (System.currentTimeMillis() < startMillis)
                            yield();

                        int thisIteration = 1;
                        try {
                            for (; thisIteration <= iterations; thisIteration++)
                            {
                                // go go go!
                                runner.run();
                            }
                        } catch (Throwable error) {
                            synchronized (exceptions) {
                                // embed the exception into something that gives
                                // us some more information about the threading
                                // environment
                                exceptions.add(new ThreadingException("thread="
                                    + this.toString()
                                    + ";threadNum=" + thisThread
                                    + ";maxThreads=" + threads
                                    + ";iteration=" + thisIteration
                                    + ";maxIterations=" + iterations, error));
                            }
                        }
                    }
                };
        }

        // start the serial tests(does not spawn the threads)
        for (int i = 0; i < serialCount; i++) {
            runners[0].run();
        }

        // start the multithreaded
        for (int i = 0; i < threads; i++) {
            runners[i].start();
        }

        // wait for them all to complete
        for (int i = 0; i < threads; i++) {
            try {
                runners[i].join();
            } catch (InterruptedException e) {
            }
        }

        if (exceptions.size() == 0)
            return; // sweeeeeeeet: no errors

        // embed all the exceptions that were throws into a
        // ThreadingException
        Throwable [] errors = (Throwable []) exceptions.toArray(
            new Throwable [0]);
        throw new ThreadingException("The "
            + errors.length + " embedded errors "
            + "occured in the execution of " + iterations + " iterations "
            + "of " + threads + " threads: [" + title + "]", errors);
    }

    /**
     * Check to see if we are in the top-level execution stack.
     */
    public boolean isRootThread() {
        return multiThreadExecuting == null;
    }

    /**
     * A Runnable that can throw an Exception: used to test cases.
     */
    public static interface VolatileRunnable {

        public void run() throws Exception;
    }

    /**
     * Exception for errors caught during threading tests.
     */
    public class ThreadingException extends RuntimeException {

        private final Throwable[] _nested;

        public ThreadingException(String msg, Throwable nested) {
            super(msg);
            if (nested == null)
                _nested = new Throwable[0];
            else
                _nested = new Throwable[]{ nested };
        }

        public ThreadingException(String msg, Throwable[] nested) {
            super(msg);
            if (nested == null)
                _nested = new Throwable[0];
            else
                _nested = nested;
        }

        public void printStackTrace() {
            printStackTrace(System.out);
        }

        public void printStackTrace(PrintStream out) {
            printStackTrace(new PrintWriter(out));
        }

        public void printStackTrace(PrintWriter out) {
            super.printStackTrace(out);
            for (int i = 0; i < _nested.length; i++) {
                out.print("Nested Throwable #" + (i + 1) + ": ");
                _nested[i].printStackTrace(out);
            }
        }
    }

    /**
     * Return the last method name that called this one by
     * parsing the current stack trace.
     *
     * @param exclude a method name to skip
     * @throws IllegalStateException If the calling method could not be
     * identified.
     * @author Marc Prud'hommeaux
     */
    public String callingMethod(String exclude) {
        // determine the currently executing method by
        // looking at the stack track. Hackish, but convenient.
        StringWriter sw = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(sw));
        for (StringTokenizer stackTrace = new StringTokenizer(sw.toString(),
            System.getProperty("line.separator"));
            stackTrace.hasMoreTokens();) {
            String line = stackTrace.nextToken().trim();

            // not a stack trace element
            if (!(line.startsWith("at ")))
                continue;

            String fullMethodName = line.substring(0, line.indexOf("("));

            String shortMethodName = fullMethodName.substring(
                fullMethodName.lastIndexOf(".") + 1);

            // skip our own methods!
            if (shortMethodName.equals("callingMethod"))
                continue;
            if (exclude != null && shortMethodName.equals(exclude))
                continue;

            return shortMethodName;
        }

        throw new IllegalStateException("Could not identify calling "
            + "method in stack trace");
    }

    //////////
    // Timing
    //////////

    /**
     * Sleep the current Thread for a random amount of time from 0-1000 ms.
     */
    public void sleepRandom() {
        sleepRandom(1000);
    }

    /**
     * Sleep the current Thread for a random amount of time from
     * 0-<code>max</code> ms.
     */
    public void sleepRandom(int max) {
        try {
            Thread.currentThread().sleep((long) (Math.random() * max));
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Re-run this method in the current thread, timing out
     * after the specified number of seconds.
     * Usage:
     * <pre> public void timeOutOperation() { if (timeout(5 * 1000)) return;
     *  Thread.currentThread().sleep(10 * 1000); }
     * </pre>
     * <p/>
     * <p/>
     * <strong>Warning</strong> this method should be used sparingly,
     * and only when you expect that a timeout will <strong>not</strong>
     * occur. It utilized the deprecated {@link Thread#stop()} and
     * {@link Thread#interrupt} methods, which can leave monitors in an
     * invalid state. It is only used because it provides more
     * meaningful information than just seeing that the entire autobuild
     * timed out.
     *
     * @param millis the number of milliseconds we should wait.
     * @return true if we are are in the thread that requested the
     *         timeout, false if we are in the timeout thread itself.
     */
    public boolean timeout(long millis) throws Throwable {
        String methodName = callingMethod("timeout");
        return timeout(millis, methodName);
    }

    /**
     * @see #timeout(long)
     */
    public boolean timeout(long millis, String methodName) throws Throwable {
        // we are in the timing out-thread: do nothing so the
        // actual test method can run
        if (inTimeoutThread)
            return false;

        inTimeoutThread = true;
        long endTime = System.currentTimeMillis() + millis;

        try {
            final Method method = getClass().
                getMethod(methodName, (Class[]) null);
            final Object thz = this;

            // spawn thread
            TimeOutThread tot = new TimeOutThread("TimeOutThread ["
                + methodName + "] (" + millis + "ms)") {
                public void run() {
                    try {
                        method.invoke(thz, (Object[]) null);
                    } catch (Throwable t) {
                        throwable = t;
                    } finally {
                        completed = true;
                    }
                }
            };

            tot.start();

            // wait for the completion or a timeout to occur
            tot.join(millis);

            // have we timed out? Kill the thread and throw an exception
            if (System.currentTimeMillis() >= endTime) {
                // if we are waiting on a monitor, this will give
                // us a useful stack trace.
                try {
                    tot.interrupt();
                } catch (Throwable e) {
                }
                Thread.currentThread().sleep(500);

                // try to kill the thread
                try {
                    tot.stop();
                } catch (Throwable e) {
                }
                Thread.currentThread().sleep(500);

                throw new OperationTimedOutException("Execution of \""
                    + methodName + "\" timed out after "
                    + millis + " milliseconds", tot.throwable);
            }

            // throw any exceptions that may have occured
            if (tot.throwable != null)
                throw tot.throwable;

            // I guess everything was OK
            return true;
        } finally {
            inTimeoutThread = false;
        }
    }

    private static class TimeOutThread extends Thread {

        public Throwable throwable = null;
        public boolean completed = false;

        public TimeOutThread(String name) {
            super(name);
            setDaemon(true);
        }
    }

    /**
     * Indicates that a timeout occured.
     */
    public static class OperationTimedOutException extends RuntimeException {

        private final Throwable _err;

        public OperationTimedOutException(String msg, Throwable throwable) {
            super(msg);
            _err = throwable;
        }

        public void printStackTrace() {
            printStackTrace(System.out);
        }

        public void printStackTrace(PrintStream out) {
            printStackTrace(new PrintWriter(out));
        }

        public void printStackTrace(PrintWriter out) {
            super.printStackTrace(out);
            if (_err != null) {
                out.print("Nested Throwable: ");
                _err.printStackTrace(out);
            }
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
}
