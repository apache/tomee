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
<%-- ====================================================================  --%>
<%-- Executes a query which is determined by the request parameter.        --%>
<%-- Displays the results                                                  --%>
<%-- ====================================================================  --%>
<%@include file="header.jsp"%>


<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Book"%>
<%@page import="openbook.domain.Author"%>
<%@page import="openbook.util.JSPUtility"%>
<%@page import="java.util.List"%>

<div id="help">
   <h3>Query Result</h3>
   
   This page is displaying the result of the Criteria query specified in the previous Search page.
   <ul>
   <li><B>Readability</B>: Criteria query is more powerful than JPQL but hardly as readable.
   JPA specification says nothing about how a Criteria Query should be displayed.
   OpenJPA implementation provides a simple 
   <a HREF="generated-html/openbook/server/OpenBookServiceImpl.java.html#getQuery" type="popup">
   <code>toString()</code></a> method to display a Criteria query as a string that is quite  
   <em>similar</em> to an equivalent JPQL string. <em>Similar</em> because Criteria query can
   express certain queries that has no equivalent JPQL counterpart. 
   </li>
   <li><B>Eager Fetching</B>: The query result displays the Author names as well, though the query 
   had only selected the Books. But the Authors have also been fetched because
   many-to-many <a href="generated-html/openbook/domain/Book.java.html#authors" type="popup">
   Book-Author relationship</a> is annotated to be <em>eagerly fetched</em>.
   </li>
   </ul>
</div>


<div id="content" style="width: 600px; display: block">
<%!
     /**
      * Concatenates the names of the given list of Authors.
      *
      */
     public static String namesOf(List<Author> authors) {
       StringBuilder names = new StringBuilder();
       if (authors == null)
           return names.toString();
       for (Author a : authors) {
           if (names.length() != 0) names.append(", ");
           names.append(a.getName());
       }
       return names.toString();
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

<%
  String title    = request.getParameter(FORM_TITLE);
  Double minPrice = JSPUtility.toDouble(request.getParameter(FORM_PRICE_MIN));
  Double maxPrice = JSPUtility.toDouble(request.getParameter(FORM_PRICE_MAX));
  String author   = request.getParameter(FORM_AUTHOR);
  
  List<Book> books = service.select(title, minPrice, maxPrice, author);
  String query = service.getQuery(title, minPrice, maxPrice, author);
%>
Query : <code><%= query %></code>
<p></p>
<%
   if (books.isEmpty()) {
%>
    did not select any book. 
    <p><A HREF="<%= PAGE_SEARCH %>">Search again</A></p>
<%       
    return;
   }
%>
<br>
<table>
  <caption>Selected <%= books.size() %> Book<%= books.size() == 1 ? "" : "s" %></caption>
  <thead>
    <tr>
      <th width="08em">ISBN</th> 
      <th width="10em">Title</th> 
      <th width="06em">Price</th> 
      <th width="20em">Authors</th> 
      <th width="16em">Add to Cart</th>
    </tr>
  </thead>
  <tfoot>
  </tfoot>
  <tbody>
<%
  int i = 0;
  for (Book book : books) {
      session.setAttribute(book.getISBN(), book);
%>
   <TR class="<%= i++%2 == 0 ? ROW_STYLE_EVEN : ROW_STYLE_ODD %>">
      <TD> <%= book.getISBN() %> </TD>
      <TD> <%= book.getTitle() %> </TD>
      <TD> <%= JSPUtility.format(book.getPrice()) %> </TD>
      <TD> <%= namesOf(book.getAuthors()) %> </TD>
      <TD> <A HREF="<%= JSPUtility.encodeURL(PAGE_CART, 
             KEY_ACTION, ACTION_ADD, KEY_ISBN, book.getISBN()) %>">
             <img src="images/add-to-cart.gif" width="106px" height="22px" border="0"></A></TD>
   </TR>
<%
  }
%>
  </tbody>
</table>
<p>

</div>
<%@include file="footer.jsp"%>
