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
 * Unless required by applicable law or agEmployee_Last_Name to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.sequence;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * @author Tim McConnell
 * @since 2.0.0
 */
public class TestSequence extends SingleEMFTestCase {

    private String multiThreadExecuting = null;
    private static final int NUMBER_ENTITIES = 5000;

    public void setUp() {
        setUp(EntityPerson.class, EntityEmployee.class, CLEAR_TABLES,
            "openjpa.Multithreaded", "true");
    }

    public void testMultiThreadedNativeSequences() throws Exception {
        boolean supportsNativeSequence = false;

        try {
            supportsNativeSequence = ((JDBCConfiguration) emf
                .getConfiguration()).getDBDictionaryInstance()
                .nextSequenceQuery != null;
        } catch (Throwable t) {
            supportsNativeSequence = false;
        }

        if (supportsNativeSequence) {
            mttest(6, 8);
            switch ((int) (Math.random() * 7)) {
            case 0:
                createAndRemove();
                break;
            case 1:
                createManyPersonsInSeparateTransactions();
                break;
            case 2:
                createManyEmployeesInSeparateTransactions();
                break;
            case 3:
                createManyPersonsAndEmployeesInSeparateTransactions();
                break;
            case 4:
                createManyPersonsInSingleTransaction();
                break;
            case 5:
                createManyEmployeesInSingleTransaction();
                break;
            case 6:
                createManyPersonsAndEmployeesInSingleTransaction();
                break;
            }
        }
    }

    private void createAndRemove() {
        int person_id;
        int employee_id;

        EntityManager em = emf.createEntityManager();

        EntityPerson person = new EntityPerson();
        person.setFirstName("Person_First_Name");
        person.setLastName("Person_Last_Name");

        EntityEmployee employee = new EntityEmployee();
        employee.setFirstName("Employee_First_Name");
        employee.setLastName("Employee_Last_Name");
        employee.setSalary(NUMBER_ENTITIES);

        em.getTransaction().begin();
        em.persist(person);
        em.persist(employee);
        em.getTransaction().commit();

        em.refresh(person);
        em.refresh(employee);
        person_id = person.getId();
        employee_id = employee.getId();

        person = em.find(EntityPerson.class, person_id);
        assertTrue(person != null);
        assertTrue(person.getId() == person_id);
        assertTrue(person.getFirstName().equals("Person_First_Name"));
        assertTrue(person.getLastName().equals("Person_Last_Name"));

        employee = em.find(EntityEmployee.class, employee_id);
        assertTrue(employee != null);
        assertTrue(employee.getId() == employee_id);
        assertTrue(employee.getFirstName().equals("Employee_First_Name"));
        assertTrue(employee.getLastName().equals("Employee_Last_Name"));
        assertTrue(employee.getSalary() == NUMBER_ENTITIES);

        em.getTransaction().begin();
        em.remove(person);
        em.remove(employee);
        em.getTransaction().commit();

        em.clear();
        em.close();
    }

    private void createManyPersonsInSeparateTransactions() {
        EntityManager em = emf.createEntityManager();

        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityPerson person = new EntityPerson();
            person.setFirstName("1_First_name_" + ii);
            person.setLastName("1_Last_name_" + ii);

            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        }

        em.clear();
        em.close();
    }

    private void createManyEmployeesInSeparateTransactions() {
        EntityManager em = emf.createEntityManager();

        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityEmployee employee = new EntityEmployee();
            employee.setFirstName("2_First_name_" + ii);
            employee.setLastName("2_Last_name_" + ii);
            employee.setSalary(ii);

            em.getTransaction().begin();
            em.persist(employee);
            em.getTransaction().commit();
        }

        em.clear();
        em.close();
    }

    private void createManyPersonsAndEmployeesInSeparateTransactions() {
        EntityManager em = emf.createEntityManager();

        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityPerson person = new EntityPerson();
            person.setFirstName("3_First_name_" + ii);
            person.setLastName("3_Last_name_" + ii);

            EntityEmployee employee = new EntityEmployee();
            employee.setFirstName("4_First_name_" + ii);
            employee.setLastName("4_Last_name_" + ii);
            employee.setSalary(ii);

            em.getTransaction().begin();
            em.persist(person);
            em.persist(employee);
            em.getTransaction().commit();
        }

        em.clear();
        em.close();
    }

    private void createManyPersonsInSingleTransaction() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityPerson person = new EntityPerson();
            person.setFirstName("5_First_name_" + ii);
            person.setLastName("5_Last_name_" + ii);

            em.persist(person);
        }
        em.getTransaction().commit();

        em.clear();
        em.close();
    }

    private void createManyEmployeesInSingleTransaction() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityEmployee employee = new EntityEmployee();
            employee.setFirstName("6_First_name_" + ii);
            employee.setLastName("6_Last_name_" + ii);
            employee.setSalary(ii);

            em.persist(employee);
        }
        em.getTransaction().commit();

        em.clear();
        em.close();
    }

    private void createManyPersonsAndEmployeesInSingleTransaction() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        for (int ii = 0; ii < NUMBER_ENTITIES; ii++) {
            EntityPerson person = new EntityPerson();
            person.setFirstName("7_First_name_" + ii);
            person.setLastName("7_Last_name_" + ii);

            EntityEmployee employee = new EntityEmployee();
            employee.setFirstName("8_First_name_" + ii);
            employee.setLastName("8_Last_name_" + ii);
            employee.setSalary(ii);

            em.persist(person);
            em.persist(employee);
        }
        em.getTransaction().commit();

        em.clear();
        em.close();
    }

    /**
     * Re-execute the invoking method a random number of times in a random
     * number of Threads.
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
     * Execute the calling method <code>iterations</code> times in
     * <code>threads</code> Threads.
     */
    public void mttest(int threads, int iterations) {
        mttest(0, threads, iterations);
    }

    public void mttest(int serialCount, int threads, int iterations)
        throws ThreadingException {
        String methodName = callingMethod("mttest");
        mttest(serialCount, threads, iterations, methodName, new Object[0]);
    }

    /**
     * Execute a test method in multiple threads.
     * 
     * @param threads
     *            the number of Threads to run in
     * @param iterations
     *            the number of times the method should be execute in a single
     *            Thread
     * @param method
     *            the name of the method to execute
     * @param args
     *            the arguments to pass to the method
     * @throws ThreadingException
     *             if an errors occur in any of the Threads. The actual
     *             exceptions will be embedded in the exception. Note that this
     *             means that assert() failures will be treated as errors rather
     *             than warnings.
     * @author Marc Prud'hommeaux
     */
    public void mttest(int threads, int iterations, final String method,
        final Object[] args) throws ThreadingException {
        mttest(0, threads, iterations, method, args);
    }

    public void mttest(int serialCount, int threads, int iterations,
        final String method, final Object[] args) throws ThreadingException {
        if (multiThreadExecuting != null 
            && multiThreadExecuting.equals(method)) {
            // we are currently executing in multi-threaded mode:
            // don't deadlock!
            return;
        }

        multiThreadExecuting = method;

        try {
            Class<?>[] paramClasses = new Class[args.length];
            for (int i = 0; i < paramClasses.length; i++)
                paramClasses[i] = args[i].getClass();

            final Method meth;

            try {
                meth = getClass().getMethod(method, paramClasses);
            } catch (NoSuchMethodException nsme) {
                throw new ThreadingException(nsme.toString(), nsme);
            }

            final Object thiz = this;

            mttest("reflection invocation: (" + method + ")", serialCount,
                threads, iterations, new VolatileRunnable() {
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
     * @param title
     *            a description of the test, for inclusion in the error message
     * @param serialCount
     *            the number of times to run the method serially before spawning
     *            threads.
     * @param threads
     *            the number of Threads to run in
     * @param iterations
     *            the number of times the method should
     * @param runner
     *            the VolatileRunnable that will execute the actual test from
     *            within the Thread.
     * @throws ThreadingException
     *             if an errors occur in any of the Threads. The actual
     *             exceptions will be embedded in the exception. Note that this
     *             means that assert() failures will be treated as errors rather
     *             than warnings.
     * @author Marc Prud'hommeaux
     */
    public void mttest(String title, final int serialCount, final int threads,
        final int iterations, final VolatileRunnable runner)
        throws ThreadingException {
        final List exceptions = Collections.synchronizedList(new LinkedList());

        Thread[] runners = new Thread[threads];

        final long startMillis = System.currentTimeMillis() + 1000;

        for (int i = 1; i <= threads; i++) {
            final int thisThread = i;

            runners[i - 1] = new Thread(title + " [" + i + " of " + threads
                + "]") {
                public void run() {
                    // do our best to have all threads start at the exact
                    // same time. This is imperfect, but the closer we
                    // get to everyone starting at the same time, the
                    // better chance we have for identifying MT problems.
                    while (System.currentTimeMillis() < startMillis)
                        yield();

                    int thisIteration = 1;
                    try {
                        for (; thisIteration <= iterations; thisIteration++) {
                            // go go go!
                            runner.run();
                        }
                    } catch (Throwable error) {
                        synchronized (exceptions) {
                            // embed the exception into something that gives
                            // us some more information about the threading
                            // environment
                            exceptions.add(new ThreadingException("thread="
                                + this.toString() + ";threadNum=" + thisThread
                                + ";maxThreads=" + threads + ";iteration="
                                + thisIteration + ";maxIterations="
                                + iterations, error));
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
        Throwable[] errors = (Throwable[]) exceptions.toArray(new Throwable[0]);
        throw new ThreadingException("The " + errors.length
            + " embedded errors " + "occured in the execution of " + iterations
            + " iterations " + "of " + threads + " threads: [" + title + "]",
            errors);
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

        private static final long serialVersionUID = -1911769845552507956L;
        private final Throwable[] _nested;

        public ThreadingException(String msg, Throwable nested) {
            super(msg);
            if (nested == null)
                _nested = new Throwable[0];
            else
                _nested = new Throwable[] { nested };
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
     * Return the last method name that called this one by parsing the current
     * stack trace.
     * 
     * @param exclude
     *            a method name to skip
     * @throws IllegalStateException
     *             If the calling method could not be identified.
     * @author Marc Prud'hommeaux
     */
    public String callingMethod(String exclude) {
        // determine the currently executing method by
        // looking at the stack track. Hackish, but convenient.
        StringWriter sw = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(sw));
        for (StringTokenizer stackTrace = new StringTokenizer(sw.toString(),
            System.getProperty("line.separator"))
            ; stackTrace.hasMoreTokens() ; ) {
            String line = stackTrace.nextToken().trim();

            // not a stack trace element
            if (!(line.startsWith("at ")))
                continue;

            String fullMethodName = line.substring(0, line.indexOf("("));

            String shortMethodName = fullMethodName.substring(fullMethodName
                .lastIndexOf(".") + 1);

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
}
