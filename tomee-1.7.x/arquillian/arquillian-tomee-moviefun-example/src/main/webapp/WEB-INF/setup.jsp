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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>

<h2>Setup</h2>
Done!

<h2>Seeded Database with the Following movies</h2>
<table width="500">
    <tr>
        <td><b>Title</b></td>
        <td><b>Director</b></td>
        <td><b>Genre</b></td>
    </tr>
	<c:forEach var="movie" items="${movies}">    
	<tr>
        <td><c:out value="${movie.title}" />
        </td>
        <td><c:out value="${movie.director}" />
        </td>
        <td><c:out value="${movie.genre}" />
        </td>
    </tr>
    </c:forEach>
</table>

<h2>Continue</h2>
<a href="moviefun">Go to main app</a>