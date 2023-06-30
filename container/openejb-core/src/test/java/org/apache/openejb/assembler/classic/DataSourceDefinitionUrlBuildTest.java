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

import org.apache.openejb.config.event.DataSourceDefinitionUrlBuild;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(ApplicationComposer.class)
@ContainerProperties({
        @ContainerProperties.Property(name = "test", value = "new://Service?class-name=org.apache.openejb.assembler.classic.DataSourceDefinitionUrlBuildTest$DataSourceModifier"),
})
public class DataSourceDefinitionUrlBuildTest {

    private static final String CHANGED_JDBC_URL = "jdbc:hsqldb:mem:adjustedDS";

    @EJB
    private DatasourceDefinitionBean uniqueDataSource;

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{DatasourceDefinitionBean.class};
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

        public void validProperties() throws SQLException {
            final DbcpManagedDataSource dbcp = DbcpManagedDataSource.class.cast(ds);
            assertEquals(2, dbcp.getMinIdle());
            assertTrue(dbcp.isPoolPreparedStatements());

            try(final Connection c = dbcp.getConnection()) {
                assertEquals(CHANGED_JDBC_URL, c.getMetaData().getURL());
            }
        }
    }

    @Test
    public void assertDataSourceDefinition() throws Exception {
        assertDataSourceDefinitionValues(uniqueDataSource.getDs());
        uniqueDataSource.validProperties();
    }

    private void assertDataSourceDefinitionValues(final DataSource dataSource) throws Exception {
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
    }

    public static class DataSourceModifier {
        public void enhance(@Observes final DataSourceDefinitionUrlBuild event) {
            assertNotNull(event);
            assertNotNull(event.getDataSource());
            final org.apache.openejb.jee.DataSource ds = event.getDataSource();
            ds.setUrl(CHANGED_JDBC_URL);
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
