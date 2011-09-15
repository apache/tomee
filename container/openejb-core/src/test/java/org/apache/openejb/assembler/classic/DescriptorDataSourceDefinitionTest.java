package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
                .className("org.hsqldb.jdbc.jdbcDataSource")
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
                .className("org.hsqldb.jdbc.jdbcDataSource")
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
                .className("org.hsqldb.jdbc.jdbcDataSource")
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
        assertDataSourceDefinitionValues(orange.getDs(), "org.hsqldb.jdbc.jdbcDataSource", "sa", "");
    }

    @Test
    public void assertDatasourceDefinitions() throws Exception {
        assertDataSourceDefinitionValues(yellow.getMega(), "org.hsqldb.jdbc.jdbcDataSource", "foo1", "bar1");
        assertDataSourceDefinitionValues(yellow.getGiga(), "org.hsqldb.jdbc.jdbcDataSource", "foo2", "bar2");
    }

    private void assertDataSourceDefinitionValues(DataSource dataSource, String clazz, String user, String password) throws Exception {
        assertNotNull("injection should work", dataSource);

        Movies movies = new Movies(dataSource);

        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

        List<Movie> list = movies.getMovies();
        assertEquals("List.size()", 3, list.size());

        for (Movie movie : list) {
            movies.deleteMovie(movie);
        }

        assertEquals("Movies.getMovies()", 0, movies.getMovies().size());

        Connection connection = dataSource.getConnection();
        connection.prepareStatement("DROP TABLE movie").execute();
    }

    public static class Movies {
        private final DataSource movieDatabase;

        public Movies(DataSource movieDatabase) throws SQLException {
            this.movieDatabase = movieDatabase;

            Connection connection = movieDatabase.getConnection();
            PreparedStatement stmt = connection.prepareStatement("CREATE TABLE movie ( director VARCHAR(255), title VARCHAR(255), year integer)");
            stmt.execute();

        }

        public void addMovie(Movie movie) throws Exception {
            Connection conn = movieDatabase.getConnection();
            try {
                PreparedStatement sql = conn.prepareStatement("INSERT into movie (director, title, year) values (?, ?, ?)");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
            } finally {
                conn.close();
            }
        }


        public void deleteMovie(Movie movie) throws Exception {
            Connection conn = movieDatabase.getConnection();
            try {
                PreparedStatement sql = conn.prepareStatement("DELETE from movie where director = ? AND title = ? AND year = ?");
                sql.setString(1, movie.getDirector());
                sql.setString(2, movie.getTitle());
                sql.setInt(3, movie.getYear());
                sql.execute();
            } finally {
                conn.close();
            }
        }

        public List<Movie> getMovies() throws Exception {
            ArrayList<Movie> movies = new ArrayList<Movie>();
            Connection conn = movieDatabase.getConnection();
            try {
                PreparedStatement sql = conn.prepareStatement("SELECT director, title, year from movie");
                ResultSet set = sql.executeQuery();
                while (set.next()) {
                    Movie movie = new Movie();
                    movie.setDirector(set.getString("director"));
                    movie.setTitle(set.getString("title"));
                    movie.setYear(set.getInt("year"));
                    movies.add(movie);
                }

            } finally {
                conn.close();
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

        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }
}
