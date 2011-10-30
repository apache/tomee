Title: EJB Webservice

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/ejb-webservice) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/ejb-webservice). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## Calculator

    package org.superbiz.ws;
    
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    
    @Stateless
    @WebService(portName = "CalculatorPort",
            serviceName = "CalculatorWebService",
            targetNamespace = "http://superbiz.org/wsdl")
    public class Calculator {
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    }

## web.xml

    <web-app xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
             metadata-complete="false"
             version="2.5">
    
    </web-app>
    
