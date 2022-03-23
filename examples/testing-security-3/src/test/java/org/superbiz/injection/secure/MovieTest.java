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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Properties;

//START SNIPPET: code
public class MovieTest {

    @EJB
    private Movies movies;

    private EJBContainer container;

    private Context getContext(String user, String pass) throws NamingException {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        p.setProperty("openejb.authentication.realmName", "ServiceProviderLogin");
        p.put(Context.SECURITY_PRINCIPAL, user);
        p.put(Context.SECURITY_CREDENTIALS, pass);
        return new InitialContext(p);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        this.container = EJBContainer.createEJBContainer(p);
        this.container.getContext().bind("inject", this);
    }

    @After
    public void tearDown() {
        this.container.close();
    }

    @Test
    public void testAsManager() throws Exception {
        final Context context = getContext("paul", "michelle");

        try {
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            Assert.assertEquals("List.size()", 3, list.size());

            for (Movie movie : list) {
                movies.deleteMovie(movie);
            }

            Assert.assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
        } finally {
            context.close();
        }
    }

    @Test
    public void testAsEmployee() throws Exception {
        final Context context = getContext("eddie", "jump");

        try {
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            Assert.assertEquals("List.size()", 3, list.size());

            for (Movie movie : list) {
                try {
                    movies.deleteMovie(movie);
                    Assert.fail("Employees should not be allowed to delete");
                } catch (EJBAccessException e) {
                    // Good, Employees cannot delete things
                }
            }

            // The list should still be three movies long
            Assert.assertEquals("Movies.getMovies()", 3, movies.getMovies().size());
        } finally {
            context.close();
        }
    }

    @Test
    public void testUnauthenticated() throws Exception {
        try {
            movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            Assert.fail("Unauthenticated users should not be able to add movies");
        } catch (EJBAccessException e) {
            // Good, guests cannot add things
        }

        try {
            movies.deleteMovie(null);
            Assert.fail("Unauthenticated users should not be allowed to delete");
        } catch (EJBAccessException e) {
            // Good, Unauthenticated users cannot delete things
        }

        try {
            // Read access should be allowed
            movies.getMovies();
        } catch (EJBAccessException e) {
            Assert.fail("Read access should be allowed");
        }
    }

    @Test
    public void testLoginFailure() throws NamingException {
        try {
            getContext("eddie", "panama");
            Assert.fail("supposed to have a login failure here");
        } catch (javax.naming.AuthenticationException e) {
            //expected
        }

        try {
            getContext("jimmy", "foxylady");
            Assert.fail("supposed to have a login failure here");
        } catch (javax.naming.AuthenticationException e) {
            //expected
        }
    }
}
//END SNIPPET: code
