<%@ page import="
org.acme.movie.JndiContext,
java.sql.Connection,
javax.naming.Context,
java.sql.Statement,
java.util.Date,
org.acme.movie.MovieEntity,
java.text.SimpleDateFormat,
javax.sql.DataSource,
java.util.Collection,
java.util.Iterator,
org.acme.movie.Movie"%>

<h2>Setup</h2>
<%
    //Looking up the DataSource from JNDI
    Context context = JndiContext.LOCAL;
    DataSource dataSource = (DataSource) context.lookup("jdbc/moviedb");

    //Opening a connection to the moviedb database<br>
    Connection connection = dataSource.getConnection();
    Statement statement = connection.createStatement();

    //Dropping old MOVIE table
    statement.execute("DROP TABLE movie");

    //Creating a new MOVIE table
    statement.execute(" CREATE TABLE movie (\n"+
                     "  movieId       int PRIMARY KEY,\n" +
                     "  title         varchar(200) not null,\n" +
                     "  director      varchar(200) not null,\n" +
                     "  genre         varchar(200) not null,\n" +
                     "  rating        int not null,\n" +
                     "  release_date  date not null\n" +
                     ")");
    //Closing connections
    statement.close();
    connection.close();

    //Creating an initial set of records using CMP Entities
    SimpleDateFormat date = MovieEntity.DATE_FORMAT;

    MovieEntity.Home.create("Wedding Crashers", "David Dobkin", "Comedy", 7 , date.parse("2005.07.15"));
    MovieEntity.Home.create("Starsky & Hutch", "Todd Phillips", "Action", 6 , date.parse("2004.03.05"));
    MovieEntity.Home.create("Shanghai Knights", "David Dobkin", "Action", 6 , date.parse("2003.07.13"));
    MovieEntity.Home.create("I-Spy", "Betty Thomas", "Adventure", 5 , date.parse("2002.11.01"));
    MovieEntity.Home.create("The Royal Tenenbaums", "Wes Anderson", "Comedy", 8 , date.parse("2001.12.14"));
    MovieEntity.Home.create("Zoolander", "Ben Stiller", "Comedy", 6 , date.parse("2001.09.28"));
    MovieEntity.Home.create("Shanghai Noon", "Tom Dey", "Comedy", 7 , date.parse("2000.05.26"));
    //Done!
%>
Done!

<h2>Seeded Database with the Following movies</h2>
<table width="500">
<tr>
<td><b>Title</b></td>
<td><b>Director</b></td>
<td><b>Genre</b></td>
</tr>
<%
    Collection movies = MovieEntity.Home.findAllMovies();
    for (Iterator iterator = movies.iterator(); iterator.hasNext();) {
        Movie movie = (Movie) iterator.next();
%>
<tr>
<td><%=movie.getTitle()%></td>
<td><%=movie.getDirector()%></td>
<td><%=movie.getGenre()%></td>
</tr>

<%
    }
%>
</table>

<h2>Continue</h2>
<a href="index.jsp">Go to main app</a>