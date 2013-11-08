<%-- 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
--%>
<!-- ===========================================================      -->
<!--      This JSP page demonstrates usage of form-based query.       -->
<!-- ===========================================================      -->
<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Book"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@include file="header.jsp"%>

<div id="help">
  <h3>Criteria Query & Form-based Search</h3>
  
  This is a typical search form in a web page. The user fills in one or more fields
  about a Book, clicks <b>Search</b> and a set of Books matching
  the user criteria appear on the web page.
  <br>
  <ul>
  <li><b>Dynamic Query</b>:
  Behind the page, the user input will be used to build up a query, executed on a database and the results
  returned. The problem is how to build the right query based on the fields that the user had filled in. 
  If there are 6 input fields -- potentially there are <code>2<sup>6</sup>=64</code>
  ways to fill in the form and, hence, 64 possible queries. 
  <br>
  <b>Criteria Query</b> -- introduced in JPA 2.0 -- can solve this combinatorial problem 
  by building the query  
  <a href="generated-html/openbook/server/OpenBookServiceImpl.java.html#buildQuery" type="popup">
  <em>dynamically</em></a>. 
  <br>
  The code shows how the predicates are created based on availability of particular input fields.
  In the end, all the predicates are anded together to create the final selection criteria.
  </li>
  <li><b>Safety by Strong Typing</b>: This new query API is also strongly typed via usage of generic 
  type arguments. 
  For example, the API signature enforces that the type of the result returned by a query must match 
  the type of arguments selected. Or, a String field can not be compared by mistake against a numeric
  value. All these new features reduces the risk of runtime errors that can be caused by String-based
  query.   
  </li>
  </ul>
  <br>
  More about Criteria Query can be found 
  <A href="http://www.ibm.com/developerworks/java/library/j-typesafejpa/" target="_blank">here</A>.
</div>

<div id="content" style="width: 600px; display: block">
<%!
     public static String getParameter(HttpServletRequest request, String param) {
          return getParameter(request, param, true);
     }
     public static String getParameter(HttpServletRequest request, String param, boolean replaceNull) {
       String value = request.getParameter(param);
       return replaceNull ? (value == null ? "" : value) : value;
   }
%>

<% 
   OpenBookService service = (OpenBookService)session.getAttribute(KEY_SERVICE); 
   if (service == null) {
%>
       <jsp:forward page="<%= PAGE_HOME %>"></jsp:forward>
<%
   }
%>

<br>
Fill in the details for a book you are searching for. 
<br>
<form method="GET" action="<%= PAGE_BOOKS %>">
  Title : <br> <input type="text" name="<%= FORM_TITLE %>" value="<%= getParameter(request, FORM_TITLE) %>" 
                      style="width:20em"><br>
  Author: <br> <input type="text" name="<%= FORM_AUTHOR %>" value="<%= getParameter(request, FORM_AUTHOR) %>" 
                      style="width:20em"><br>
  Price from : <input type="text" name="<%= FORM_PRICE_MIN %>" value="<%= getParameter(request, FORM_PRICE_MIN) %>" 
                      style="width:6em"> to 
               <input type="text" name="<%= FORM_PRICE_MAX %>" value="<%= getParameter(request, FORM_PRICE_MIN) %>" 
                      style="width:6em"><br>
  <br>
  <input type="submit" src="images/search.gif" width="60px" height="22px" border="0">
</form>
<p></p>
<b>Search Tips</b>: 
   <ol>
   <li>You can leave one, more or all fields empty.</li>
   <li>OpenBooks database currently contains <%= service.count(Book.class) %> books.</li>
   <li>Book titles are <code>Book-1</code>, <code>Book-2</code>, <code>Book-3</code>,...</li>
   <li>Author names are <code>Author-1</code>, <code>Author-2</code>, <code>Author-3</code>,...</li>
   <li>Both Book and Author names accept wildcard characters. <br>
   For example, an underscore like <code>Book-3_</code> will match any single character to return 
   <code>Book-31</code>, <code>Book-32</code>, <code>Book-33</code> ...
    
   </ol>
  
</div>
<%@include file="footer.jsp"%>
