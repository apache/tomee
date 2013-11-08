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
<!--      This JSP page demonstrates usage of OpenBookService to purchase Books.       -->
<!-- ===============================================================================================      -->
<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Book"%>
<%@page import="openbook.domain.ShoppingCart"%>
<%@page import="openbook.domain.PurchaseOrder"%>
<%@page import="openbook.domain.LineItem"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="openbook.util.JSPUtility"%>

<%@include file="header.jsp"%>

<div id="help">
<h3>Composite Relation and Derived Identity</h3>

  You have just created a  
  <a href="generated-html/openbook/domain/PurchaseOrder.java.html#init" type="popup">new Purchase Order 
  </a>.  Each Book in 
  the Shopping Cart is turned into separate line item for the order and the Purchase
  Order and all its line items are inserted as new database records. All this happened with
  this <a href="generated-html/openbook/server/OpenBookServiceImpl.java.html#placeOrder" type="popup">
  few lines of Java Code</a> 
  <br>
  <ul>
  <li><b>Transitive Persistence</b>:
  The line items are persisted without any <em>explicit</em> call to persist because persist operation 
  <a href="generated-html/openbook/domain/PurchaseOrder.java.html#items" type="popup"> cascades 
  via the order-line item relation</a>. During persist, the JPA provider generated a new
  <a href="generated-html/openbook/domain/PurchaseOrder.java.html#id" type="popup"> identity of 
  the Purchase Order</a> automatically. 
  </li>
  <li><b>Compound, Derived identity</b>:
  The identity generation, in this case, is more interesting if you look at the 
  <a href="generated-html/openbook/domain/LineItem.java.html#id" type="popup">
  identity used by the line items</a>. Line Item uses <em>compound, derived</em>
  identity. It is <em>compound</em> because more than one field make up the identity.
  It is <em>derived</em> because one of the identity field borrows its value
  from the owning Purchase Order's identity. Because of such dependency, the persistent
  identity of a Line Item can only be assigned only after a new 
  identity value for a Purchase Order gets generated during transaction commit.    
  </li>
  <li><b>Composite Relation</b>: Purchase Order - Line Item relationship is by semantics,
  a composite relation. Simply stated, life time of a Line Item is completely controlled
  by the owning Purchase Order. It is noteworthy that the Line Item constructor is
  package protected to ensure that only Purchase Order can create them.
  <br>
  The Java language provides no support for composite
  relationship -- but it is a common pattern for persistent objects. The new JPA features
  of derived identity combined with orphan delete (another new feature) and cascaded 
  persistent operations provides an application to reliably express a classic Master-Details
  pattern in their application.     
  </li>
  </ul>
  
</div>

<div id="content" style="width: 600px; display: block">
<% 
   OpenBookService service = (OpenBookService)session.getAttribute(KEY_SERVICE); 
   ShoppingCart cart = (ShoppingCart)session.getAttribute(KEY_CART);
   PurchaseOrder order = null;
   if (cart.isEmpty()) {
%>
        <jsp:forward page="<%=PAGE_SEARCH%>"/>
<% 
   } else {
         order = service.placeOrder(cart);
   }
%>
<h3>Thank you for ordering from OpenBooks</h3>
<p>
<table>
  <caption>Order : <%= order.getId() %> on <%= JSPUtility.format(order.getPlacedOn()) %> 
     for <%= order.getItems().size() %> Book<%= order.getItems().size() == 0 ? "" : "s" %>
  </caption>
  <thead>
    <tr>
      <th width="10em">Title</th> 
      <th width="04em">Quantity</th>
      <th width="06em">Price</th> 
    </tr>
  </thead>
  <tfoot>
    <tr>
      <td><A HREF="<%= PAGE_SEARCH %>">Continue Shopping</A></td>
    </tr>
  </tfoot>
  <tbody>
<%
  int i = 0;
  List<LineItem> items = order.getItems();
  for (LineItem item : items) {
%>
   <TR class="<%= i++%2 == 0 ? ROW_STYLE_EVEN : ROW_STYLE_ODD %>">
      <TD> <%= item.getBook().getTitle() %> </TD>
      <TD> <%= item.getQuantity() %> </TD>
      <TD> <%= JSPUtility.format(item.getBook().getPrice() * item.getQuantity()) %> </TD>
   </TR>
<%
  }
%>
  <TR>
  <TD>Total</TD><TD> </TD><TD><%= JSPUtility.format(order.getTotal()) %></TD>
  </TR>
  </tbody>
</table>


</div>
<%@include file="footer.jsp"%>
