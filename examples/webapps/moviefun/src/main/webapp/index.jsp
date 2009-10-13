<%--
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
--%>    
<%@ page import="org.superbiz.moviefun.Movie,
                 org.superbiz.moviefun.Movies,
                 javax.naming.InitialContext,
                 java.util.List,
                 java.util.ListIterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

    InitialContext initialContext = new InitialContext();
    Movies moviesBean = (Movies) initialContext.lookup("java:comp/env/movies");

    List movies = null;
    ListIterator listIterator = null;
    int display = 5;
    String action = request.getParameter("action");

    if ("Add".equals(action)) {

        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String genre = request.getParameter("genre");
        int rating = Integer.parseInt(request.getParameter("rating"));
        int year = Integer.parseInt(request.getParameter("year"));

        Movie movie = new Movie(title, director, genre, rating, year);

        moviesBean.addMovie(movie);

    } else if ("Remove".equals(action)) {

        String[] ids = request.getParameterValues("id");
        for (String id : ids) {
            moviesBean.deleteMovieId(new Long(id));
        }

    } else if (">>".equals(action)) {

        movies = (List) session.getAttribute("movies.collection");
        listIterator = (ListIterator) session.getAttribute("movies.iterator");

    } else if ("<<".equals(action)) {

        movies = (List) session.getAttribute("movies.collection");
        listIterator = (ListIterator) session.getAttribute("movies.iterator");
        for (int i = display * 2; i > 0 && listIterator.hasPrevious(); i--) {
            listIterator.previous(); // backup
        }

    } else if ("findByTitle".equals(action)) {

        movies = moviesBean.findByTitle(request.getParameter("key"));

    } else if ("findByDirector".equals(action)) {

        movies = moviesBean.findByDirector(request.getParameter("key"));

    } else if ("findByGenre".equals(action)) {

        movies = moviesBean.findByGenre(request.getParameter("key"));
    }

    if (movies == null) {
        try {
            movies = moviesBean.getMovies();
        } catch (Throwable e) {
            // We must not have run setup yet
            response.sendRedirect("setup.jsp");
            return;
        }
    }

    if (listIterator == null) {
        listIterator = movies.listIterator();
    }

    session.setAttribute("movies.collection", movies);
    session.setAttribute("movies.iterator", listIterator);
%>
<html>
<head><title>Moviefun :: Index</title>
<link rel="stylesheet" href="default.css" type="text/css" />

</head>
<body>
<p/>
<div id="Content">
<table>
<tr>
<td>
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr class="topBar">
        <td align="left" width="85%">&nbsp;
            <span class="topBarDiv">Mini-Movie Application</span>
        </td>
        <td align="right" valign="middle" width="1%" nowrap>
            <form method="POST" action="index.jsp" name="findMovie" style="padding: 1px; margin: 1px">
            <select name="action">
                <option value="findByTitle">Title</option>
                <option value="findByDirector">Director</option>
                <option value="findByGenre">Genre</option>
            </select>
            <input type="text" name="key" size="20"/>
            <input type="submit" value="Search"/>
            </form>
        </td>
    </tr>
    </table>

</td>
</tr>
<tr>
<td>
    <div class="basicPanelContainer" style="width: 100%">

        <div class="basicPanelTitle">Movies</div>

        <div class="basicPanelBody">
            <form method="POST" action="index.jsp" name="listMovies" style="padding: 1px; margin: 1px">
            <table class="tableview" width="100%" cellspacing="0" cellpadding="0" style="padding: 5px">
            <tr>
            <th>Title</th>
            <th>Director</th>
            <th>Genre</th>
            <th>Rating</th>
            <th>Year</th>
            <th>&nbsp;</th>
            </tr>
            <%
            int start = listIterator.nextIndex();
            for (int i=display; i > 0 && listIterator.hasNext(); i-- ) {
                Movie movie = (Movie) listIterator.next();
            %>
            <tr>
            <td width="200"><%=movie.getTitle()%></td>
            <td width="120"><%=movie.getDirector()%></td>
            <td width="90"><%=movie.getGenre()%></td>
            <td width="50"><%=movie.getRating()%></td>
            <td width="50"><%=movie.getYear()%></td>
            <td><input type="checkbox" name="id" value="<%=movie.getId()%>"></td>
            </tr>

            <% } %>
            </table>
            <table width="100%" cellspacing="0" cellpadding="0" style="padding: 5px">
            <tr>
            <td>
            <% if (start!=0&&listIterator.hasPrevious()){%><input type="submit" name="action" value="<<"/><%}%>

            <%=start+1%> - <%=listIterator.nextIndex()%> of <%=movies.size()%>

            <% if (listIterator.hasNext()){%><input type="submit" name="action" value=">>"/><%}%>
            </td>
            <td align="right">
                <input type="submit" name="action" value="Remove"/>
            </td>
            </tr>
            </table>
            </form>
        </div>
    </div>
</td>
</tr>
<tr>
<td>
    <div class="basicPanelContainer" style="width: 100%">
        <div class="basicPanelTitle">Add</div>
        <div class="basicPanelBody">
            <form method="POST" action="index.jsp" name="addMovie" style="padding: 1px; margin: 1px">
            <table width="100%" cellspacing="0" cellpadding="0" style="padding: 0px">
            <tr>
            <td width="200"><input type="text" name="title" size="29"/></td>
            <td width="120"><input type="text" name="director" size="17"/></td>
            <td width="90"><input type="text" name="genre" size="14"/></td>
            <td width="50"><input type="text" name="rating" size="7"/></td>
            <td width="50"><input type="text" name="year" size="4"/></td>
            <td><input type="submit" name="action" value="Add"/></td>
            </tr>
            </table>
            </form>

        </div>
    </div>
</td>
</tr>
</table>


    <div class="bottomshadow"></div>

    <div id="poweredby" class="smalltext">
        Powered by
        <a href="http://tomcat.apache.org" class="smalltext">Apache Tomcat</a> and
        <a href="http://openejb.apache.org" class="smalltext">Apache OpenEJB</a>.
        <br/>
    </div>


</div>
</body>
</html>