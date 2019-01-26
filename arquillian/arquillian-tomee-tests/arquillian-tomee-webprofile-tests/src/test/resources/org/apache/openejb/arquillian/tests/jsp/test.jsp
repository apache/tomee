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