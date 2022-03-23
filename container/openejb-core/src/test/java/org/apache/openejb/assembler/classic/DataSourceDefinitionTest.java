/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Note: to make this test work under JavaSE 6 you should add geronimo-annotation_1.1_spec in your endorsed dir.
 * It is automatically done with maven.
 */
@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionTest {

    @EJB
    private DatasourceDefinitionBean uniqueDataSource;
    @EJB
    private DatasourceDefinitionsBean multipleDatasources;

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{DatasourceDefinitionBean.class, DatasourceDefinitionsBean.class};
    }

    @DataSourceDefinition(
        name = "java:comp/env/superDS",
        className = "org.hsqldb.jdbc.JDBCDataSource",
        user = "sa",
        password = "",
        url = "jdbc:hsqldb:mem:superDS",
        properties = {"poolPreparedStatements = true", "minIdle = 2", "maxOpenPreparedStatements = 20"}
    )
    @Singleton
    public static class DatasourceDefinitionBean {
        @Resource(name = "java:comp/env/superDS")
        private DataSource ds;

        public DataSource getDs() {
            return ds;
        }

        public void validProperties() {
            final DbcpManagedDataSource dbcp = DbcpManagedDataSource.class.cast(ds);
            assertEquals(2, dbcp.getMinIdle());
            assertTrue(dbcp.isPoolPreparedStatements());
        }
    }

    @DataSourceDefinitions({
        @DataSourceDefinition(
            name = "java:comp/env/superMegaDS",
            className = "org.hsqldb.jdbc.JDBCDataSource",
            user = "sa",
            password = "",
            url = "jdbc:hsqldb:mem:superDS"
        ),
        @DataSourceDefinition(
            name = "java:comp/env/superGigaDS",
            className = "org.hsqldb.jdbc.JDBCDataSource",
            user = "sa",
            password = "",
            url = "jdbc:hsqldb:mem:superDS"

        )
    })
    @Stateless
    public static class DatasourceDefinitionsBean {
        @Resource(name = "java:comp/env/superMegaDS")
        private DataSource mega;
        @Resource(name = "java:comp/env/superGigaDS")
        private DataSource giga;

        public DataSource getMega() {
            return mega;
        }

        public DataSource getGiga() {
            return giga;
        }
    }

    @Test
    public void assertDataSourceDefinition() throws Exception {
        assertDataSourceDefinitionValues(uniqueDataSource.getDs(), "org.hsqldb.jdbc.JDBCDataSource", "sa", "");
        uniqueDataSource.validProperties();
    }

    @Test
    public void assertDatasourceDefinitions() throws Exception {
        assertDataSourceDefinitionValues(multipleDatasources.getMega(), "org.hsqldb.jdbc.JDBCDataSource", "foo1", "bar1");
        assertDataSourceDefinitionValues(multipleDatasources.getGiga(), "org.hsqldb.jdbc.JDBCDataSource", "foo2", "bar2");
    }

    private void assertDataSourceDefinitionValues(final DataSource dataSource, final String clazz, final String user, final String password) throws Exception {
        assertNotNull("injection should work", dataSource);

        final Movies movies = new Movies(dataSource);

        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

        final List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());

        for (final Movie movie : list) {
            movies.deleteMovie(movie);
        }

        assertEquals("Movies.getMovies()", 0, movies.getMovies().size());

        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        connection.prepareStatement("DROP TABLE movie").execute();
//        assertEquals("configuration should be ok - class", "org.hsqldb.jdbc.jdbcDataSource", dataSource.getClass().getName());
//        assertEqualsByReflection("configuration should be ok - user", dataSource, "user", user);
//        assertEqualsByReflection("configuration should be ok - password", dataSource, "password", password);
    }

    private void assertEqualsByReflection(final String message, final Object value, final String name, final Object expected) throws Exception {
        final Class<?> clazz = value.getClass();
        final Field field = clazz.getDeclaredField(name);
        final boolean acc = field.isAccessible();
        if (!acc) {
            field.setAccessible(true);
        }
        try {
            final Object fieldValue = field.get(value);
            assertEquals(message, expected, fieldValue);
        } finally {
            field.setAccessible(acc);
        }

    }

    public static class Movies {
        private final DataSource movieDatabase;

        public Movies(final DataSource movieDatabase) throws SQLException {
            this.movieDatabase = movieDatabase;

            final Connection connection = movieDatabase.getConnection();
            final PreparedStatement stmt = connection.prepareStatement("CREATE TABLE movie ( director VARCHAR(255), title VARCHAR(255), year integer)");
            stmt.execute();
            stmt.close();
            connection.close();

        }

        public void addMovie(final Movie movie) throws Exception {
            try (Connection conn = movieDatabase.getConnection()) {
                final PreparedStatement sql = conn.prepareStatement("INSERT into movie (director, title, year) values (?, ?, ?)");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
                sql.close();
            }
        }


        public void deleteMovie(final Movie movie) throws Exception {
            try (Connection conn = movieDatabase.getConnection()) {
                final PreparedStatement sql = conn.prepareStatement("DELETE from movie where director = ? AND title = ? AND year = ?");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
                sql.close();
            }
        }

        public List<Movie> getMovies() throws Exception {
            final ArrayList<Movie> movies = new ArrayList<>();
            try (Connection conn = movieDatabase.getConnection()) {
                final PreparedStatement sql = conn.prepareStatement("SELECT director, title, year from movie");
                final ResultSet set = sql.executeQuery();
                while (set.next()) {
                    final Movie movie = new Movie();
                    movie.setDirector(set.getString("director"));
                    movie.setTitle(set.getString("title"));
                    movie.setYear(set.getInt("year"));
                    movies.add(movie);
                }

            }
            return movies;
        }
    }

    public static class Movie {
        private String director;
        private String title;
        private int year;

        public Movie() {
        }

        public Movie(final String director, final String title, final int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(final String director) {
            this.director = director;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public int getYear() {
            return year;
        }

        public void setYear(final int year) {
            this.year = year;
        }
    }
}
