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
package org.superbiz.injection.secure;

import junit.framework.TestCase;
import org.superbiz.injection.secure.api.RunAsEmployee;
import org.superbiz.injection.secure.api.RunAsManager;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.Stateless;
import jakarta.ejb.embeddable.EJBContainer;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

//START SNIPPET: code

public class MovieTest extends TestCase {

    @EJB
    private Movies movies;

    @EJB(beanName = "ManagerBean")
    private Caller manager;

    @EJB(beanName = "EmployeeBean")
    private Caller employee;

    protected void setUp() throws Exception {
        Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        EJBContainer.createEJBContainer(p).getContext().bind("inject", this);
    }

    public void testAsManager() throws Exception {
        manager.call(new Callable() {
            public Object call() throws Exception {

                movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
                movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());

                for (Movie movie : list) {
                    movies.deleteMovie(movie);
                }

                assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
                return null;
            }
        });
    }

    public void testAsEmployee() throws Exception {
        employee.call(new Callable() {
            public Object call() throws Exception {

                movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
                movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());

                for (Movie movie : list) {
                    try {
                        movies.deleteMovie(movie);
                        fail("Employees should not be allowed to delete");
                    } catch (EJBAccessException e) {
                        // Good, Employees cannot delete things
                    }
                }

                // The list should still be three movies long
                assertEquals("Movies.getMovies()", 3, movies.getMovies().size());
                return null;
            }
        });
    }

    public void testUnauthenticated() throws Exception {
        try {
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            fail("Unauthenticated users should not be able to add movies");
        } catch (EJBAccessException e) {
            // Good, guests cannot add things
        }

        try {
            movies.deleteMovie(null);
            fail("Unauthenticated users should not be allowed to delete");
        } catch (EJBAccessException e) {
            // Good, Unauthenticated users cannot delete things
        }

        try {
            // Read access should be allowed

            List<Movie> list = movies.getMovies();

        } catch (EJBAccessException e) {
            fail("Read access should be allowed");
        }

    }

    public interface Caller {

        public <V> V call(Callable<V> callable) throws Exception;
    }

    /**
     * This little bit of magic allows our test code to execute in
     * the desired security scope.
     */

    @Stateless
    @RunAsManager
    public static class ManagerBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

    @Stateless
    @RunAsEmployee
    public static class EmployeeBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

}
//END SNIPPET: code
