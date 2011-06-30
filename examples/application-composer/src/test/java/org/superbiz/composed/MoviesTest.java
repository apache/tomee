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
package org.superbiz.composed;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.Properties;

//START SNIPPET: code
@RunWith(ApplicationComposer.class)
public class MoviesTest extends TestCase {

    @EJB
    private Movies movies;

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext
    private EntityManager entityManager;

    @Module
    public PersistenceUnit persistence() {
        PersistenceUnit unit = new PersistenceUnit("movie-unit");
        unit.setJtaDataSource("movieDatabase");
        unit.setNonJtaDataSource("movieDatabaseUnmanaged");
        unit.getClazz().add(Movie.class.getName());
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        return unit;
    }

    @Module
    public EjbJar beans() {
        EjbJar ejbJar = new EjbJar("movie-beans");
        ejbJar.addEnterpriseBean(new StatefulBean(MoviesImpl.class));
        return ejbJar;
    }

    @Configuration
    public Properties config() throws Exception {
        Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        return p;
    }

    @Test
    public void test() throws Exception {

        userTransaction.begin();

        try {
            entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
            entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
            entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));

            List<Movie> list = movies.getMovies();
            assertEquals("List.size()", 3, list.size());

            for (Movie movie : list) {
                movies.deleteMovie(movie);
            }

            assertEquals("Movies.getMovies()", 0, movies.getMovies().size());

        } finally {
            userTransaction.commit();
        }
    }
}
//END SNIPPET: code
