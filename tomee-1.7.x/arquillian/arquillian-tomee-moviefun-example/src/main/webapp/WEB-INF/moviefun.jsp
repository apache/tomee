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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>

<html>
<head><title>Moviefun :: Index</title>
    <link rel="stylesheet" href="default.css" type="text/css"/>

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
            <form method="POST" action="moviefun" name="findMovie" style="padding: 1px; margin: 1px">
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
            <form method="POST" action="moviefun" name="listMovies" style="padding: 1px; margin: 1px">
            <table class="tableview" width="100%" cellspacing="0" cellpadding="0" style="padding: 5px">
            <tr>
            <th>Title</th>
            <th>Director</th>
            <th>Genre</th>
            <th>Rating</th>
            <th>Year</th>
            <th>&nbsp;</th>
            </tr>
            <c:forEach var="movie" items="${movies}">
            <tr>
            <td width="200"><c:out value="${movie.title}" /></td>
            <td width="120"><c:out value="${movie.director}" /></td>
            <td width="90"><c:out value="${movie.genre}" /></td>
            <td width="50"><c:out value="${movie.rating}" /></td>
            <td width="50"><c:out value="${movie.year}" /></td>
            <td><input type="checkbox" name="id" value="<c:out value="${movie.id}" />"></td>
            </tr>

            </c:forEach>
            </table>
            <table width="100%" cellspacing="0" cellpadding="0" style="padding: 5px">
            <tr>
            <td>
            <c:if test="${hasPrev}"><input type="submit" name="action" value="<<"/></c:if>

            <c:out value="${start+1}" /> -  <c:out value="${end}" /> of  <c:out value="${total}" />

            <c:if test="${hasNext}"><input type="submit" name="action" value=">>"/></c:if>
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
            <form method="POST" action="moviefun" name="addMovie" style="padding: 1px; margin: 1px">
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