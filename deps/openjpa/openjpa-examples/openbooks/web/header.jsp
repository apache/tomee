<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
<!-- ========================================================================= -->
<!-- This layout page is included in every page of OpenBooks web application   -->
<!-- The layout splits the screen into four divisions                          -->
<!--     header : a header section                                             -->
<!--     left   : houses the navigation menu items                             -->
<!--     content: houses JSP page content                                      -->
<!--     footer : a footer section                                             -->
<!-- This page has not properly closed its HTML or BODY tag. The tags are      -->
<!-- closed by the footer page                                                 -->
<!-- ========================================================================= -->

<HTML>

<HEAD>
<title>OpenBooks: A sample JPA web application</title>
<link   type="text/css" rel="stylesheet" href="openbooks.css">
<script type="text/javascript" src="openbooks.js"></script>
</HEAD>

<body>
<%! 
    /**
     * Lookup keys for Session/Requeust data used throught pages.
     */
    public static String KEY_USER    = "user";
    public static String KEY_SERVICE = "OpenBookService";
    public static String KEY_CART    = "cart";
    public static String KEY_ACTION  = "action";
    public static String KEY_ISBN    = "isbn";
    public static String KEY_OID     = "oid";
    
    public static String ACTION_ADD     = "add";
    public static String ACTION_DELIVER = "deliver";
    public static String ACTION_DETAILS = "details";
    
    public static String PAGE_BOOKS     = "query.jsp";
    public static String PAGE_ORDERS    = "orders.jsp";
    public static String PAGE_CART      = "cart.jsp";
    public static String PAGE_HOME      = "intro.jsp";
    public static String PAGE_LOGIN     = "register.jsp";
    public static String PAGE_SEARCH    = "search.jsp";
    public static String PAGE_CHECKOUT  = "checkout.jsp";
    
    public static String FORM_TITLE     = "title";
    public static String FORM_AUTHOR    = "author";
    public static String FORM_PRICE_MAX = "maxPrice";
    public static String FORM_PRICE_MIN = "minPrice";
    
    
    public static String ROW_STYLE_EVEN = "even";
    public static String ROW_STYLE_ODD  = "odd";
    
%>

<!-- Header division displays the title and right-justified current user name -->
<!-- and a Shopping Cart icon for active sessions                             -->
<div id="header">
    <img alt="OpenBooks Logo" src="images/OpenBooks.jpg" 
         border="0" width="25px" height="25px" 
         align="bottom"
         hspace="2em"/>
         &nbsp;&nbsp;<A HREF="."><span style="font-size: 24pt">OpenBooks</span></A>
<% 
  Object currentUser = session.getAttribute(KEY_USER);
  boolean activeSession = currentUser != null;
  if (activeSession) {
%>
     <div style="float:right;text-align: right;margin-right:1em">
           Hello, <%= currentUser.toString() %>&nbsp;&nbsp;
           <A HREF="cart.jsp"><img src="images/Add2Cart.jpg" 
              border="0" width="25px" height="25px"></A>
     </div>
<%
  }
%>
</div>

<!-- Left menu navigation displays the items based on current session status  -->

<div id="left">
<ul>
  <li><a href="intro.jsp">Welcome</a></li>
<%
  if (activeSession) {
%>
  <li><a href="search.jsp">Search Books</a></li>
  <li><a href="orders.jsp">View Orders</a></li>
<% 
  }
%>
</ul>
</div>
