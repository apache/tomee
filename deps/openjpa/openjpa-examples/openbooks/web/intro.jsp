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

<%@include file="header.jsp"%>
<div id="help">
<center><h3>Begin by entering your name.</h3></center>
OpenBooks do not ask you to register or remember one more password.
You enter a name so that OpenBooks can refer you by name.
<hr>
OpenBooks is a sample (and perhaps simple) web application to demonstrate
some of the new features of JPA 2.0 API. OpenBooks performs basic operations
such as <br>
<OL>
<LI>Search for Books using a form-base query</LI>
<LI>Place Purchase Orders for a set of Books</LI>
<LI>Show the status of pending Purchase Orders </LI>
<LI>Change the status of pending orders </LI>
</OL>

Each of these actions invokes a  service that queries or transacts against a database. 
This database interaction takes place via Java Persistence API (JPA). 
OpenBooks is designed to work with <em>any</em> JPA 2.0 compliant provider.
Currently OpenBooks is deployed with <A HREF="http://openjpa.apache.org" target="_blank">
OpenJPA 2.0</A> as its JPA provider.
<br>
As you navigate through the pages, you can browse through the <em>actual</em> code in parallel. 
For example, when you hit the <b>Enter</b> button in this page, this is the 
<a HREF="generated-html/openbook/server/OpenBookServiceImpl.java.html#login" type="popup">
corresponding Java code</a> executed on the server side.
<br>   
</div>


<div id="content" style="width: 600px; display: block">
<% 
    Object service = session.getAttribute(KEY_SERVICE);
    
    if (service == null) {
%>
<A name="login"></A>
      <form method="get" action="<%= PAGE_LOGIN %>">
        Your Name :<br> 
        <input type="text" 
               name="<%= KEY_USER %>" 
               size="40"> 
        <p> 
        <input type="image" 
               src="images/login.gif" 
               width="111px" height="22px" border="0">
      </form>
<%
    } else { 
%>
     You have already signed in, <%= session.getAttribute(KEY_USER) %>.
<%
    }
%>
</div>

<%@include file="footer.jsp"%>
