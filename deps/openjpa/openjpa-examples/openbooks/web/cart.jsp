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
<!-- ===============================================================================================      -->
<!--      This JSP page demonstrates usage of OpenBookService to browse, select and purchase Books.       -->
<!-- ===============================================================================================      -->
<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Book"%>
<%@page import="openbook.domain.Customer"%>
<%@page import="openbook.domain.ShoppingCart"%>
<%@page import="java.util.Map"%>
<%@page import="openbook.util.JSPUtility"%>

<%@include file="header.jsp"%>

<div id="help">
  <h3>Shopping Cart</h3>
<% if (ACTION_ADD.equals(request.getParameter(KEY_ACTION))) {
%>
   You have just added a Book to the cart. 
   <br>
<% 
  }  
%>
  A <a HREF="generated-html/openbook/domain/ShoppingCart.java.html#items" type="popup">Shopping Cart</a> contains 
  <a HREF="generated-html/openbook/domain/Book.java.html#class" type="popup">Books</a> that are persistent objects. 
  <a HREF="generated-html/openbook/domain/ShoppingCart.java.html#non-persistent" type="popup">
  Shopping Cart</a>  itself, however, is <em>not</em> a persistent object.
  Shopping Cart is an in-memory data structure to hold the Books in the current web session and transfer 
   it to the server when a Purchase Order is to be placed. 

</div>
<div id="content" style="width: 600px; display: block">

<% 
   OpenBookService service = (OpenBookService)session.getAttribute(KEY_SERVICE); 
   Customer customer = (Customer)session.getAttribute(KEY_USER);
   ShoppingCart cart = (ShoppingCart)session.getAttribute(KEY_CART);
   if (ACTION_ADD.equals(request.getParameter(KEY_ACTION))) {
       String isbn = request.getParameter(KEY_ISBN);
       Book book = (Book)session.getAttribute(isbn);  
       cart.addItem(book, 1);
   }
   if (cart.isEmpty()) {
%>
      <%= customer.getName() %>, your Shopping Cart is empty.<br>
      <A HREF="<%= PAGE_SEARCH %>">Continue Shopping</A>
<%    
      return;
   } 
%>


<table>
  <caption><%= cart.getTotalCount() %> Book<%= cart.getTotalCount() == 1 ? "" : "s" %> in 
         <%= customer.getName() %>'s Shopping Cart</caption>
  <thead>
    <tr>
      <th width="10em">Title</th> 
      <th width="6em">Price</th> 
      <th width="4em">Quantity</th>
    </tr>
  </thead>
  <tfoot>
    <tr>
      <td><A HREF="<%= PAGE_SEARCH %>">Continue Shopping</A></td>
      <td><A HREF="<%= PAGE_CHECKOUT %>">
          <img src="images/checkout.gif" 
               width="156px" height="27px" border="0">
          </A></td>
    </tr>
  </tfoot>
  <tbody>
<%
   
   Map<Book,Integer> books = cart.getItems();
   int i = 0;
   for (Book b : books.keySet()) {
%>
   <TR class="<%= i++%2 == 0 ? ROW_STYLE_EVEN : ROW_STYLE_ODD %>">
      <TD> <%= b.getTitle() %> </TD>
      <TD> <%= JSPUtility.format(b.getPrice()) %> </TD>
      <TD> <%= books.get(b) %> </TD>
   </TR>
<%
   }
%>
  </tbody>
</table>


</div>
<%@include file="footer.jsp"%>
