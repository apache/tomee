index-group=Unrevised
type=page
status=published
title=Simple Webservice Without Interface
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## Calculator

    package org.superbiz.calculator;
    
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    
    @Stateless
    @WebService(
            portName = "CalculatorPort",
            serviceName = "CalculatorWsService",
            targetNamespace = "http://superbiz.org/wsdl")
    public class Calculator {
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    }

## ejb-jar.xml

    <ejb-jar/>

## CalculatorTest

    package org.superbiz.calculator;
    
    import org.apache.commons.io.IOUtils;
    import org.junit.AfterClass;
    import org.junit.Before;
    import org.junit.BeforeClass;
    import org.junit.Test;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.NamingException;
    import java.net.URL;
    import java.util.Properties;
    
    import static org.junit.Assert.assertTrue;
    
    public class CalculatorTest {
        private static EJBContainer container;
    
        @BeforeClass
        public static void setUp() throws Exception {
            final Properties properties = new Properties();
            properties.setProperty("openejb.embedded.remotable", "true");
    
            container = EJBContainer.createEJBContainer(properties);
        }
    
        @Before
        public void inject() throws NamingException {
            if (container != null) {
                container.getContext().bind("inject", this);
            }
        }
    
        @AfterClass
        public static void close() {
            if (container != null) {
                container.close();
            }
        }
    
        @Test
        public void wsdlExists() throws Exception {
            final URL url = new URL("http://127.0.0.1:4204/Calculator?wsdl");
            assertTrue(IOUtils.readLines(url.openStream()).size() > 0);
            assertTrue(IOUtils.readLines(url.openStream()).toString().contains("CalculatorWsService"));
        }
    }

## ejb-jar.xml

    <ejb-jar/>
