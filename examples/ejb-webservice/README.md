index-group=Unrevised
type=page
status=published
title=EJB Webservice
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

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
    
