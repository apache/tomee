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
<%@ page import="javax.naming.InitialContext"%><%@ page import="java.util.Properties"%><%@ page import="javax.naming.Context"%><%@ page import="org.apache.openejb.arquillian.tests.jsp.DataBusinessHome"%><%@ page import="javax.rmi.PortableRemoteObject"%><%@ page import="org.apache.openejb.arquillian.tests.jsp.DataBusiness"%><%@ page import="org.apache.openejb.arquillian.tests.jsp.Data"%><%@ page contentType="text/plain;charset=UTF-8" language="java" %>
<%

    final Properties p = new Properties();
    p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

    final InitialContext initialContext = new InitialContext(p);
    Object lookup = initialContext.lookup("java:comp/env/ejb/DataBusiness");
    DataBusinessHome home = (DataBusinessHome) PortableRemoteObject.narrow(lookup, DataBusinessHome.class);

    final DataBusiness dataBusiness = home.create();
    final Data data = new Data();
    data.setSomeText("this is a test");
    Data echoedData = dataBusiness.doLogic(data);
%>
<%= echoedData.getSomeText() %>