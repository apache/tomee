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

<%@page import="openbook.server.ServiceFactory"%>
<%@page import="openbook.server.OpenBookService"%>
<%@page import="openbook.domain.Customer"%>
<%@page import="java.util.List"%>

<div id="content" style="width: 600px; display: block">

<%-- ====================================================================  --%>
<%-- Associates the current session with OpenBookService, if the request   --%>
<%-- carries user parameter. Otherwise redirects to login page.            --%>
<%-- ====================================================================  --%>
<% 
    Object user = request.getParameter(KEY_USER);
    String nextPage = PAGE_HOME;
    if (user != null && user.toString().trim().length() > 0) {
        OpenBookService service = ServiceFactory.getService("OpenBooks");
        service.initialize(null);
        Customer customer = service.login(user.toString());
        session.setAttribute(KEY_USER, customer);
        session.setAttribute(KEY_CART, customer.newCart());
        session.setAttribute(KEY_SERVICE, service);
        nextPage = PAGE_SEARCH;
    }
%>
     <jsp:forward page="<%=nextPage%>"/>

</div>
<%@include file="footer.jsp"%>
