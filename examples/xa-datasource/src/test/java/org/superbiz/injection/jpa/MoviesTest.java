/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection.jpa;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.List;
import java.util.Properties;

//START SNIPPET: code
public class MoviesTest extends TestCase {

    public void test() throws Exception {

        final Properties p = new Properties();
        p.put("movieDatabaseXA", "new://Resource?type=javax.sql.XADataSource&class-name=org.apache.derby.jdbc.EmbeddedXADataSource");
        p.put("movieDatabaseXA.DatabaseName", "test");
        p.put("movieDatabaseXA.CreateDatabase", "create");

        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.DataSourceCreator", "dbcp");
        p.put("movieDatabase.XaDataSource", "movieDatabaseXA");
        p.put("movieDatabase.JtaManaged", "true");
        p.put("movieDatabase.UserName", "admin");
        p.put("movieDatabase.Password", "admin");
        p.put("movieDatabase.MaxActive", "128");
        p.put("movieDatabase.MaxIdle", "25");
        p.put("movieDatabase.MinIdle", "10");
        p.put("movieDatabase.AccessToUnderlyingConnectionAllowed", "true");
        p.put("movieDatabase.TestOnBorrow", "false");
        p.put("movieDatabase.TestWhileIdle", "true");
        p.put("movieDatabase.TimeBetweenEvictionRuns", "1 minute");
        p.put("movieDatabase.MaxWaitTime", "0 seconds");
        p.put("movieDatabase.PoolPreparedStatements", "true");
        p.put("movieDatabase.MaxOpenPreparedStatements", "1024");
        p.put("movieDatabase.ValidationQuery", "values 1");

        p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
        p.put("movieDatabaseUnmanaged.DataSourceCreator", "dbcp");
        p.put("movieDatabaseUnmanaged.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:derby:test;create=true");
        p.put("movieDatabaseUnmanaged.UserName", "admin");
        p.put("movieDatabaseUnmanaged.Password", "admin");
        p.put("movieDatabaseUnmanaged.JtaManaged", "false");
        p.put("movieDatabaseUnmanaged.MaxActive", "128");
        p.put("movieDatabaseUnmanaged.MaxIdle", "25");
        p.put("movieDatabaseUnmanaged.MinIdle", "10");
        p.put("movieDatabaseUnmanaged.AccessToUnderlyingConnectionAllowed", "true");
        p.put("movieDatabaseUnmanaged.TestOnBorrow", "false");
        p.put("movieDatabaseUnmanaged.TestWhileIdle", "true");
        p.put("movieDatabaseUnmanaged.TimeBetweenEvictionRuns", "1 minute");
        p.put("movieDatabaseUnmanaged.MaxWaitTime", "0 seconds");
        p.put("movieDatabaseUnmanaged.PoolPreparedStatements", "true");
        p.put("movieDatabaseUnmanaged.MaxOpenPreparedStatements", "1024");
        p.put("movieDatabaseUnmanaged.ValidationQuery", "values 1");

        EJBContainer container = EJBContainer.createEJBContainer(p);
        final Context context = container.getContext();

        Movies movies = (Movies) context.lookup("java:global/xa-datasource/Movies");
        MoviesDirect moviesDirect = (MoviesDirect) context.lookup("java:global/xa-datasource/MoviesDirect");

        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());
        assertEquals(3, moviesDirect.count());


        for (Movie movie : list) {
            movies.deleteMovie(movie);
        }

        assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
        assertEquals(0, moviesDirect.count());

        container.close();
    }
}
//END SNIPPET: code
