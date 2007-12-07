<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->

<!-- $Rev$ $Date$ -->

<%@ page import="
org.superbiz.moviefun.JndiContext,
java.sql.Connection,
javax.naming.Context,
java.sql.Statement,
java.util.Date,
org.superbiz.moviefun.MovieEntity,
java.text.SimpleDateFormat,
javax.sql.DataSource,
java.util.Collection,
java.util.Iterator,
org.superbiz.moviefun.Movie"%>

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