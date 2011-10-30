Title: Simple Webservice

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/simple-webservice) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/simple-webservice). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## CalculatorImpl

    package org.superbiz.calculator;
    
    import javax.ejb.Stateless;
    import javax.jws.HandlerChain;
    import javax.jws.WebService;
    import javax.xml.ws.Holder;
    import java.util.Date;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a 2 interfaces:
     * <ul>
     * <li>CalculatorWs a webservice interface</li>
     * <li>CalculatorLocal a local interface</li>
     * </ul>
     */
    @Stateless
    @WebService(
            portName = "CalculatorPort",
            serviceName = "CalculatorWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.calculator.CalculatorWs")
    @HandlerChain(file = "handler.xml")
    public class CalculatorImpl implements CalculatorWs, CalculatorLocal {
    
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    
        public int factorial(
                int number,
                Holder<String> userId,
                Holder<String> returnCode,
                Holder<Date> datetime) {
    
            if (number == 0) {
                returnCode.value = "Can not calculate factorial for zero.";
                return -1;
            }
    
            returnCode.value = userId.value;
            datetime.value = new Date();
            return (int) factorial(number);
        }
    
        // return n!
        // precondition: n >= 0 and n <= 20
    
        private static long factorial(long n) {
            if (n < 0) throw new RuntimeException("Underflow error in factorial");
            else if (n > 20) throw new RuntimeException("Overflow error in factorial");
            else if (n == 0) return 1;
            else return n * factorial(n - 1);
        }
    }

## CalculatorLocal

    package org.superbiz.calculator;
    
    import javax.ejb.Local;
    
    @Local
    public interface CalculatorLocal extends CalculatorWs {
    }

## CalculatorWs

    package org.superbiz.calculator;
    
    import javax.jws.WebParam;
    import javax.jws.WebService;
    import javax.jws.soap.SOAPBinding;
    import javax.jws.soap.SOAPBinding.ParameterStyle;
    import javax.jws.soap.SOAPBinding.Style;
    import javax.jws.soap.SOAPBinding.Use;
    import javax.xml.ws.Holder;
    import java.util.Date;
    
    /**
     * This is an EJB 3 webservice interface
     * A webservice interface must be annotated with the @WebService
     * annotation.
     */
    @WebService(
            name = "CalculatorWs",
            targetNamespace = "http://superbiz.org/wsdl")
    public interface CalculatorWs {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    
        // because of CXF bug, BARE must be used instead of default WRAPPED
    
        @SOAPBinding(use = Use.LITERAL, parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT)
        public int factorial(
                int number,
                @WebParam(name = "userid", header = true, mode = WebParam.Mode.IN) Holder<String> userId,
                @WebParam(name = "returncode", header = true, mode = WebParam.Mode.OUT) Holder<String> returnCode,
                @WebParam(name = "datetime", header = true, mode = WebParam.Mode.INOUT) Holder<Date> datetime);
    }

## DummyInterceptor

    package org.superbiz.handler;
    
    import javax.xml.namespace.QName;
    import javax.xml.ws.handler.MessageContext;
    import javax.xml.ws.handler.soap.SOAPHandler;
    import javax.xml.ws.handler.soap.SOAPMessageContext;
    import java.util.Collections;
    import java.util.Set;
    
    public class DummyInterceptor implements SOAPHandler<SOAPMessageContext> {
        public DummyInterceptor() {
            super();
        }
    
        public Set<QName> getHeaders() {
            return Collections.emptySet();
        }
    
        public void close(MessageContext mc) {
        }
    
        public boolean handleFault(SOAPMessageContext mc) {
            return true;
        }
    
        public boolean handleMessage(SOAPMessageContext mc) {
            return true;
        }
    }

## handler.xml

    <handler-chains xmlns="http://java.sun.com/xml/ns/javaee">
      <handler-chain>
        <handler>
          <handler-name>org.superbiz.handler.DummyInterceptor</handler-name>
          <handler-class>org.superbiz.handler.DummyInterceptor</handler-class>
        </handler>
      </handler-chain>
    </handler-chains>
    

## CalculatorTest

    package org.superbiz.calculator;
    
    import junit.framework.TestCase;
    import org.apache.openejb.api.LocalClient;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.Holder;
    import javax.xml.ws.Service;
    import javax.xml.ws.WebServiceRef;
    import java.net.URL;
    import java.util.Date;
    import java.util.Properties;
    
    @LocalClient
    public class CalculatorTest extends TestCase {
    
        @WebServiceRef(
                wsdlLocation = "http://127.0.0.1:4204/CalculatorImpl?wsdl"
        )
        private CalculatorWs calculatorWs;
    
        private InitialContext initialContext;
    
        // date used to invoke a web service with INOUT parameters
        private static final Date date = new Date();
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
            initialContext.bind("inject", this);
        }

        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        public void testCalculatorViaWsInterface() throws Exception {
            Service calcService = Service.create(
                    new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl"),
                    new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
            assertNotNull(calcService);
    
            CalculatorWs calc = calcService.getPort(CalculatorWs.class);
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
    
        public void testWebServiceRefInjection() throws Exception {
            assertEquals(10, calculatorWs.sum(4, 6));
            assertEquals(12, calculatorWs.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calculatorWs.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
    
        public void testCalculatorViaRemoteInterface() throws Exception {
            CalculatorLocal calc = (CalculatorLocal) initialContext.lookup("CalculatorImplLocal");
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
    
            Holder<String> userIdHolder = new Holder<String>("jane");
            Holder<String> returnCodeHolder = new Holder<String>();
            Holder<Date> datetimeHolder = new Holder<Date>(date);
            assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
            assertEquals(userIdHolder.value, returnCodeHolder.value);
            assertTrue(date.before(datetimeHolder.value));
        }
    }

## ejb-jar.xml

    <ejb-jar/>

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.calculator.CalculatorTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/simple-webservice
    INFO - openejb.base = /Users/dblevins/examples/simple-webservice
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-webservice/target/test-classes
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-webservice/target/classes
    INFO - Beginning load: /Users/dblevins/examples/simple-webservice/target/test-classes
    INFO - Beginning load: /Users/dblevins/examples/simple-webservice/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/simple-webservice/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/simple-webservice/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/simple-webservice/classpath.ear
    INFO - Jndi(name=CalculatorImplLocal) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/simple-webservice/CalculatorImpl!org.superbiz.calculator.CalculatorLocal) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/simple-webservice/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - LocalClient(class=org.superbiz.calculator.CalculatorTest, module=test-classes) 
    INFO - Deployed Application(path=/Users/dblevins/examples/simple-webservice/classpath.ear)
    INFO - Initializing network services
    INFO - Creating ServerService(id=httpejbd)
    INFO - Creating ServerService(id=cxf)
    INFO - Creating ServerService(id=admin)
    INFO - Creating ServerService(id=ejbd)
    INFO - Creating ServerService(id=ejbds)
    INFO - Initializing network services
      ** Starting Services **
      NAME                 IP              PORT  
      httpejbd             127.0.0.1       4204  
      admin thread         127.0.0.1       4200  
      ejbd                 127.0.0.1       4201  
      ejbd                 127.0.0.1       4203  
    -------
    Ready!
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.211 sec
    
    Results :
    
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
    
