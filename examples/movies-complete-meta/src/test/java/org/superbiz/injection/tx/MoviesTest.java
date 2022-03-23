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
package org.superbiz.injection.tx;

import junit.framework.TestCase;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.embeddable.EJBContainer;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * See the transaction-rollback example as it does the same thing
 * via UserTransaction and shows more techniques for rollback
 */
//START SNIPPET: code
public class MoviesTest extends TestCase {

    @EJB
    private Movies movies;

    @EJB(beanName = "TransactionBean")
    private Caller transactionalCaller;

    @EJB(beanName = "NoTransactionBean")
    private Caller nonTransactionalCaller;

    protected void setUp() throws Exception {
        final Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        EJBContainer.createEJBContainer(p).getContext().bind("inject", this);
    }

    @Override
    protected void tearDown() throws Exception {
        transactionalCaller.call(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (final Movie m : movies.getMovies()) {
                    movies.deleteMovie(m);
                }
                return null;
            }
        });
    }

    private void doWork() throws Exception {

        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());

        for (Movie movie : list) {
            movies.deleteMovie(movie);
        }

        assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
    }

    public void testWithTransaction() throws Exception {
        transactionalCaller.call(new Callable() {
            public Object call() throws Exception {
                doWork();
                return null;
            }
        });
    }

    public void testWithoutTransaction() throws Exception {
        try {
            nonTransactionalCaller.call(new Callable() {
                public Object call() throws Exception {
                    doWork();
                    return null;
                }
            });
            fail("The Movies bean should be using TransactionAttributeType.MANDATORY");
        } catch (jakarta.ejb.EJBException e) {
            // good, our Movies bean is using TransactionAttributeType.MANDATORY as we want
        }
    }

    public static interface Caller {

        public <V> V call(Callable<V> callable) throws Exception;
    }

    /**
     * This little bit of magic allows our test code to execute in
     * the scope of a container controlled transaction.
     */
    @Stateless
    @RunAs("Manager")
    @TransactionAttribute(REQUIRES_NEW)
    public static class TransactionBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

    @Stateless
    @RunAs("Manager")
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public static class NoTransactionBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

}
//END SNIPPET: code
