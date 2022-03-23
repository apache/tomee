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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class DescriptorDataSourceDefinitionTest {

    @EJB
    private OrangeBean orange;

    @EJB
    private YellowBean yellow;

    @Module
    public EjbJar application() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final SingletonBean orange = ejbJar.addEnterpriseBean(new SingletonBean(OrangeBean.class));

        orange.getDataSource().add(new org.apache.openejb.jee.DataSource()
                .name("java:comp/env/superDS")
                .className("org.hsqldb.jdbc.JDBCDataSource")
                .user("sa")
                .password("")
                .url("jdbc:hsqldb:mem:superDS")
        );

        orange.getResourceRef().add(new ResourceRef()
                .lookup("java:comp/env/superDS")
                .injectionTarget(OrangeBean.class, "ds")
        );

        final StatelessBean yellow = ejbJar.addEnterpriseBean(new StatelessBean(YellowBean.class));

        yellow.getDataSource().add(new org.apache.openejb.jee.DataSource()
                .name("java:comp/env/superMegaDS")
                .className("org.hsqldb.jdbc.JDBCDataSource")
                .user("sa")
                .password("")
                .url("jdbc:hsqldb:mem:superDS")
        );

        yellow.getResourceRef().add(new ResourceRef()
                .lookup("java:comp/env/superMegaDS")
                .injectionTarget(YellowBean.class, "mega")
        );


        yellow.getDataSource().add(new org.apache.openejb.jee.DataSource()
                .name("java:comp/env/superGigaDS")
                .className("org.hsqldb.jdbc.JDBCDataSource")
                .user("sa")
                .password("")
                .url("jdbc:hsqldb:mem:superDS")
        );

        yellow.getResourceRef().add(new ResourceRef()
                .lookup("java:comp/env/superGigaDS")
                .injectionTarget(YellowBean.class, "giga")
        );


        return ejbJar;
    }

    public static class OrangeBean {

        private DataSource ds;

        public DataSource getDs() {
            return ds;
        }
    }

    public static class YellowBean {

        private DataSource mega;

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
        assertDataSourceDefinitionValues(orange.getDs(), "org.hsqldb.jdbc.JDBCDataSource", "sa", "");
    }

    @Test
    public void assertDatasourceDefinitions() throws Exception {
        assertDataSourceDefinitionValues(yellow.getMega(), "org.hsqldb.jdbc.JDBCDataSource", "foo1", "bar1");
        assertDataSourceDefinitionValues(yellow.getGiga(), "org.hsqldb.jdbc.JDBCDataSource", "foo2", "bar2");
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
        connection.prepareStatement("DROP TABLE movie").execute();
    }

    public static class Movies {
        private final DataSource movieDatabase;

        public Movies(final DataSource movieDatabase) throws SQLException {
            this.movieDatabase = movieDatabase;

            final Connection connection = movieDatabase.getConnection();
            final PreparedStatement stmt = connection.prepareStatement("CREATE TABLE movie ( director VARCHAR(255), title VARCHAR(255), year integer)");
            stmt.execute();

        }

        public void addMovie(final Movie movie) throws Exception {
            try (Connection conn = movieDatabase.getConnection()) {
                final PreparedStatement sql = conn.prepareStatement("INSERT into movie (director, title, year) values (?, ?, ?)");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
            }
        }


        public void deleteMovie(final Movie movie) throws Exception {
            try (Connection conn = movieDatabase.getConnection()) {
                final PreparedStatement sql = conn.prepareStatement("DELETE from movie where director = ? AND title = ? AND year = ?");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
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
