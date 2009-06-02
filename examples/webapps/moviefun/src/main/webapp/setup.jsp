<%@ page import=" java.util.Iterator"%>
<%@ page import="org.superbiz.moviefun.Movies" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.superbiz.moviefun.Movie" %>
<%@ page import="java.util.List" %>

<h2>Setup</h2>
<%
    InitialContext initialContext = new InitialContext();
    Movies moviesBean = (Movies) initialContext.lookup("java:comp/env/movies");

    moviesBean.addMovie(new Movie("Wedding Crashers", "David Dobkin", "Comedy", 7 , 2005));
    moviesBean.addMovie(new Movie("Starsky & Hutch", "Todd Phillips", "Action", 6 , 2004));
    moviesBean.addMovie(new Movie("Shanghai Knights", "David Dobkin", "Action", 6 , 2003));
    moviesBean.addMovie(new Movie("I-Spy", "Betty Thomas", "Adventure", 5 , 2002));
    moviesBean.addMovie(new Movie("The Royal Tenenbaums", "Wes Anderson", "Comedy", 8 , 2001));
    moviesBean.addMovie(new Movie("Zoolander", "Ben Stiller", "Comedy", 6 , 2001));
    moviesBean.addMovie(new Movie("Shanghai Noon", "Tom Dey", "Comedy", 7 , 2000));
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
    List<Movie> movies = moviesBean.getMovies();
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