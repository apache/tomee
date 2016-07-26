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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import java.io.File;
import java.util.Properties;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class MoviesXATest {
    @Module
    @Classes(value = { MoviesXA.class, Movies.class, MoviesDirect.class })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit jpa() {
        final PersistenceUnit unit = new PersistenceUnit("movie-unit");
        unit.setJtaDataSource("movieDatabase");
        unit.setNonJtaDataSource("movieDatabaseUnmanaged");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema");
        unit.addClass(Movie.class);
        return unit;
    }

    @Configuration
    public Properties config() {
        final String db = "target/test + " + abs(System.nanoTime());
        Files.deleteOnExit(new File(db));

        final Properties p = new Properties();

        p.put("movieDatabaseXA", "new://Resource?type=javax.sql.XADataSource&class-name=org.apache.derby.jdbc.EmbeddedXADataSource");
        p.put("movieDatabaseXA.DatabaseName", db);
        p.put("movieDatabaseXA.CreateDatabase", "create");

        p.put("movieDatabase", "new://Resource?type=DataSource");
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
        p.put("movieDatabaseUnmanaged.LogSql", "true");
        p.put("movieDatabaseUnmanaged.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:derby:" + db + ";create=true");
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

        /*

        Configuration for MS SQL Server

        p.put("movieDatabaseXA", "new://Resource?type=javax.sql.XADataSource&class-name=com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
        p.put("movieDatabaseXA.DatabaseName", "moviefun");
        p.put("movieDatabaseXA.URL", "jdbc:sqlserver://localhost:1433;databaseName=moviefun;SelectMethod=cursor;sendStringParametersAsUnicode=false");

        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.XaDataSource", "movieDatabaseXA");
        p.put("movieDatabase.UserName", "sa");
        p.put("movieDatabase.Password", "XXX");
        p.put("movieDatabase.JtaManaged", "true");
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
        p.put("movieDatabase.ValidationQuery", "select 1");

        p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
        p.put("movieDatabaseUnmanaged.LogSql", "true");
        p.put("movieDatabaseUnmanaged.JdbcDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:sqlserver://localhost:1433;databaseName=moviefun;SelectMethod=cursor;sendStringParametersAsUnicode=false");
        p.put("movieDatabaseUnmanaged.UserName", "sa");
        p.put("movieDatabaseUnmanaged.Password", "XXX");
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
        p.put("movieDatabaseUnmanaged.ValidationQuery", "select 1");

        p.put("movieDatabaseXA", "new://Resource?type=javax.sql.XADataSource&class-name=oracle.jdbc.xa.client.OracleXADataSource");
        p.put("movieDatabaseXA.url", "jdbc:oracle:thin:@//localhost:1521/orcl");

        */

        /*

        Configuration for Oracle

        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.XaDataSource", "movieDatabaseXA");
        p.put("movieDatabase.JtaManaged", "true");
        p.put("movieDatabase.UserName", "system");
        p.put("movieDatabase.Password", "oracle");
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
        p.put("movieDatabase.ValidationQuery", "select 1 from dual");

        p.put("movieDatabaseUnmanaged", "new://Resource?type=DataSource");
        p.put("movieDatabaseUnmanaged.LogSql", "true");
        p.put("movieDatabaseUnmanaged.JdbcDriver", "oracle.jdbc.driver.OracleDriver");
        p.put("movieDatabaseUnmanaged.JdbcUrl", "jdbc:oracle:thin:@//localhost:1521/orcl");
        p.put("movieDatabaseUnmanaged.UserName", "system");
        p.put("movieDatabaseUnmanaged.Password", "oracle");
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
        p.put("movieDatabaseUnmanaged.ValidationQuery", "select 1 from dual");

        */

        System.out.println("Using db: " + db);

        return p;
    }

    @EJB
    private Movies movies;

    @EJB
    private MoviesDirect moviesDirect;

    @EJB
    private MoviesXA runner;

    @Test
    public void run() throws Exception {
        movies.deleteAll();

        runner.reset();

        final Movie movie = new Movie();
        movie.setTitle("Bad Boys");
        movie.setDirector("Michael Bay");
        movie.setYear(1995);

        runner.run(movie);
        assertEquals(1, moviesDirect.count());

        final Movie storedMovie = runner.find();
        assertNotNull(storedMovie);
    }

    @Test
    public void failBefore() {
        runner.before();
        doFail();
    }

    @Test
    public void failAfter() {
        doFail();
    }

    private void doFail() {
        runner.fail();
        try {
            final Movie movie = new Movie();
            movie.setTitle("Bad Boys");
            movie.setDirector("Michael Bay");
            movie.setYear(1995);

            runner.run(movie);
        } catch (final EJBException ee) {
            System.out.flush();
            System.err.flush();
            System.out.println("Exception ->");
            ee.printStackTrace();
            System.out.println();
            System.out.flush();
            System.err.flush();
        }

        assertEquals(0, moviesDirect.count());
    }
}
