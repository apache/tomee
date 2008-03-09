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
package org.superbiz.injection.secure;

import junit.framework.TestCase;

import javax.annotation.security.RunAs;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
 */
public class MovieTest extends TestCase {
    private Context context;

    protected void setUp() throws Exception {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
        p.put("movieDatabaseUnmanaged.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        p.put("movieDatabaseUnmanaged.JtaManaged", "false");

        context = new InitialContext(p);
    }

    public void testAsManager() throws Exception {
        Caller managerBean = (Caller) context.lookup("ManagerBeanLocal");
        managerBean.call(new Callable() {
            public Object call() throws Exception {

                Movies movies = (Movies) context.lookup("MoviesLocal");

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
        Caller employeeBean = (Caller) context.lookup("EmployeeBeanLocal");
        employeeBean.call(new Callable() {
            public Object call() throws Exception {
                Movies movies = (Movies) context.lookup("MoviesLocal");

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
        Movies movies = (Movies) context.lookup("MoviesLocal");

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


    public static interface Caller {
        public <V> V call(Callable<V> callable) throws Exception;
    }

    /**
     * This little bit of magic allows our test code to execute in
     * the scope.
     * <p/>
     * The src/test/resource/META-INF/ejb-jar.xml will cause this
     * EJB to be automatically discovered and deployed when
     * OpenEJB boots up.
     */

    @Stateless
    @RunAs("Manager")
    public static class ManagerBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

    @Stateless
    @RunAs("Employee")
    public static class EmployeeBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

}
