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
<!-- ===========================================================================      -->
<!--      This JSP page demonstrates changing status of a Purchase Order.             -->
<!-- ===========================================================================      -->
<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Book"%>
<%@page import="openbook.domain.Customer"%>
<%@page import="openbook.domain.ShoppingCart"%>
<%@page import="openbook.domain.PurchaseOrder"%>
<%@page import="openbook.domain.LineItem"%>
<%@page import="openbook.util.JSPUtility"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<%@include file="header.jsp"%>


<div id="help">
<h3>Optimistic semantics and Orphan Delete</h3>

This page displays all the orders placed by the current users. 
This page also allows to 
<A HREF="generated-html/openbook/server/OpenBookServiceImpl.java.html#deliver" type="popup">
<em>deliver</em></A> an order. Delivering an order essentially amounts to
decrementing the inventory for each line item, 
<A href="generated-html/openbook/domain/PurchaseOrder.java.html#setDelivered" type="popup">changing the status</A>
which, as an interesting side-effect, nullifies the Line Items.
<ul>
<li><b>Optimistic Semantics</b>: Delivery is one of the operations that may fail due to
optimistic transaction model used by OpenBooks and which is also the default transaction model
proposed in JPA. The optimistic transaction model promoted that an Order can <em>always</em>
be placed, even if the inventory is inadequate. Only while fulfilling the order in a separate
transaction, the insufficient inventory may fail to deliver an order. 
</li>
<li><b>Orphan Delete</b>: JPA 2.0 had added support for composite relation via new orphan delete
functionality. To demonstrate its effect, on delivery an Order nullifies its Line Items. As a 
result, the Line Items gets deleted from the database as they are no more referred. That is why,
for pending orders, you can see their line items -- but once an order is delivered its line items
are no more available.  
</li>
</ul>
</div>

<div id="content" style="width: 600px; display: block">

<% 
   OpenBookService service = (OpenBookService)session.getAttribute(KEY_SERVICE); 
   if (service == null) {
%>
       <jsp:forward page="<%= PAGE_HOME %>"></jsp:forward>
<%
   }
   if (ACTION_DELIVER.equals(request.getParameter(KEY_ACTION))) {
       String oid = request.getParameter(KEY_OID);
       PurchaseOrder order = (PurchaseOrder)session.getAttribute(oid);
       service.deliver(order);
   }
   
   Customer customer = (Customer)session.getAttribute(KEY_USER);
   List<PurchaseOrder> pendingOrders   = service.getOrders(PurchaseOrder.Status.DELIVERED, customer);
   List<PurchaseOrder> deliveredOrders = service.getOrders(PurchaseOrder.Status.PENDING, customer);
   List<PurchaseOrder> orders = new ArrayList<PurchaseOrder>(pendingOrders);
   orders.addAll(deliveredOrders);
   if (orders.isEmpty()) {
%>
       <%= customer.getName() %>, you have not placed any order yet.<br>
<%
       return;
   }
%>


  
<table>
  <caption><%= customer.getName() %>, you have placed <%= orders.size() %> (
  <%= pendingOrders.size() == 0 ? "none" : "" + pendingOrders.size()%> pending,
  <%= deliveredOrders.size() == 0 ? " none" : " " + deliveredOrders.size()%> delivered) orders 
  </caption>
  <thead>
    <tr>
      <th width="06em">ID</th> 
      <th width="04em">Total</th> 
      <th width="10em">Placed On</th> 
      <th width="08em">Status</th> 
      <th width="10em">Delivered On</th> 
      <th width="08em">Deliver</th>
    </tr>
  </thead>
  <tfoot>
  </tfoot>
  <tbody>
<%
  int i = 0;
  for (PurchaseOrder order : orders) {
      session.setAttribute(""+order.getId(), order);
%>
   <TR class="<%= i++%2 == 0 ? ROW_STYLE_EVEN : ROW_STYLE_ODD %>">
      <TD> <A HREF="<%= JSPUtility.encodeURL(PAGE_ORDERS, 
              KEY_ACTION, ACTION_DETAILS, 
              KEY_OID, 
              order.getId()) %>"> <%= order.getId() %></A></TD>
      <TD> <%= order.getTotal() %> </TD>
      <TD> <%= JSPUtility.format(order.getPlacedOn()) %> </TD>
      <TD> <%= order.getStatus() %> </TD>
<% 
    if (order.isDelivered()) {
%>        
      <TD> <%= JSPUtility.format(order.getDeliveredOn()) %> </TD>
      <TD> </TD>
<%
    } else {
%>
      <TD>  </TD>
      <TD> <A HREF="<%= JSPUtility.encodeURL(PAGE_ORDERS, KEY_ACTION, ACTION_DELIVER, 
                  KEY_OID, order.getId()) %>">
                  <img src="images/orders.gif" width="156px" height="27px" border="0"></A></TD>
<%        
    }
%>
   </TR>
<%
  }
%>
  </tbody>
</table>
<p></p>

<%
  if (ACTION_DETAILS.equals(request.getParameter(KEY_ACTION))) {
      String oid = request.getParameter(KEY_OID);
      PurchaseOrder order = (PurchaseOrder)session.getAttribute(oid);
      List<LineItem> items = order.getItems();
      if (order.isDelivered()) {
         if (items != null && items.isEmpty()) {
%>
             Order <%= order.getId() %> has been delivered. Line items of delivered orders are automatically
             deleted due to orphan delete nature of Master-Details relationship. 
<%        } else {
%>
             Order <%= order.getId() %> has been delivered but still contains line items. 
             This is an implementation error because delivered orders must have at least one line item by design.
<%
          }
      } else {
%>               
        <table>
          <caption>Total of <%= items.size() %> Book<%= items.size() == 0 ? "" : "s" %> 
                   in Order <%= order.getId() %>
          </caption>
          <thead>
            <tr>
              <th width="10em">Title</th> 
              <th width="06em">Price</th> 
              <th width="04em">Quantity</th>
              <th width="06em">Cost</th> 
            </tr>
          </thead>
          <tbody>
<%
          int j = 0;
          for (LineItem item : items) {
%>
            <TR class="<%= j++%2 == 0 ? ROW_STYLE_EVEN : ROW_STYLE_ODD %>">
              <TD> <%= item.getBook().getTitle() %> </TD>
              <TD> <%= JSPUtility.format(item.getBook().getPrice()) %> </TD>
              <TD> <%= item.getQuantity() %> </TD>
              <TD> <%= JSPUtility.format(item.getBook().getPrice() * item.getQuantity()) %> </TD>
            </TR>
<%
           }
%>
          <TR>
            <TD>Total</TD><TD></TD><TD></TD>
            <TD><%= JSPUtility.format(order.getTotal()) %></TD>
          </TR>
          </tbody>
        </table>
<%
      }
  }
%>
       


</div>


<%@include file="footer.jsp"%>
