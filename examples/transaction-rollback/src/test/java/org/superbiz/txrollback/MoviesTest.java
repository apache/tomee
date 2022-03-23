/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.txrollback;

import junit.framework.TestCase;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.RollbackException;
import jakarta.transaction.UserTransaction;
import java.util.List;
import java.util.Properties;

//START SNIPPET: code
public class MoviesTest extends TestCase {

    @EJB
    private Movies movies;

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext
    private EntityManager entityManager;

    private EJBContainer ejbContainer;

    public void setUp() throws Exception {
        Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb" + System.currentTimeMillis());

        ejbContainer = EJBContainer.createEJBContainer(p);
        ejbContainer.getContext().bind("inject", this);
    }

    @Override
    protected void tearDown() throws Exception {
        ejbContainer.close();
    }

    /**
     * Standard successful transaction scenario.  The data created inside
     * the transaction is visible after the transaction completes.
     *
     * Note that UserTransaction is only usable by Bean-Managed Transaction
     * beans, which can be specified with @TransactionManagement(BEAN)
     */
    public void testCommit() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

        } finally {
            userTransaction.commit();
        }

        // Transaction was committed
        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());

    }

    /**
     * Standard transaction rollback scenario.  The data created inside
     * the transaction is not visible after the transaction completes.
     */
    public void testUserTransactionRollback() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

        } finally {
            userTransaction.rollback();
        }

        // Transaction was rolled back
        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 0, list.size());

    }

    /**
     * Transaction is marked for rollback inside the bean via
     * calling the jakarta.ejb.SessionContext.setRollbackOnly() method
     *
     * This is the cleanest way to make a transaction rollback.
     */
    public void testMarkedRollback() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

            movies.callSetRollbackOnly();
        } finally {
            try {
                userTransaction.commit();
                fail("A RollbackException should have been thrown");
            } catch (RollbackException e) {
                // Pass
            }
        }

        // Transaction was rolled back
        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 0, list.size());

    }

    /**
     * Throwing an unchecked exception from a bean will cause
     * the container to call setRollbackOnly() and discard the
     * bean instance from further use without calling any @PreDestroy
     * methods on the bean instance.
     */
    public void testExceptionBasedRollback() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

            try {
                movies.throwUncheckedException();
            } catch (RuntimeException e) {
                // Good, this will cause the tx to rollback
            }
        } finally {
            try {
                userTransaction.commit();
                fail("A RollbackException should have been thrown");
            } catch (RollbackException e) {
                // Pass
            }
        }

        // Transaction was rolled back
        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 0, list.size());

    }

    /**
     * It is still possible to throw unchecked (runtime) exceptions
     * without dooming the transaction by marking the exception
     * with the @ApplicationException annotation or in the ejb-jar.xml
     * deployment descriptor via the <application-exception> tag
     */
    public void testCommit2() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

            try {
                movies.throwApplicationException();
            } catch (RuntimeException e) {
                // This will *not* cause the tx to rollback
                // because it is marked as an @ApplicationException
            }
        } finally {
            userTransaction.commit();
        }

        // Transaction was committed
        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());

    }
}
//END SNIPPET: code
