index-group=Unrevised
type=page
status=published
title=Webservice Security
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## CalculatorImpl

    package org.superbiz.calculator;
    
    import javax.annotation.security.DeclareRoles;
    import javax.annotation.security.RolesAllowed;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a single interface: CalculatorWs a webservice interface.
     */
    //START SNIPPET: code
    @DeclareRoles(value = {"Administrator"})
    @Stateless
    @WebService(
            portName = "CalculatorPort",
            serviceName = "CalculatorWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.calculator.CalculatorWs")
    public class CalculatorImpl implements CalculatorWs, CalculatorRemote {
    
        @RolesAllowed(value = {"Administrator"})
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        @RolesAllowed(value = {"Administrator"})
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    }

## CalculatorRemote

    package org.superbiz.calculator;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface CalculatorRemote {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    }

## CalculatorWs

    package org.superbiz.calculator;
    
    import javax.jws.WebService;
    
    //END SNIPPET: code
    
    /**
     * This is an EJB 3 webservice interface
     * A webservice interface must be annotated with the @Local
     * annotation.
     */
    //START SNIPPET: code
    @WebService(targetNamespace = "http://superbiz.org/wsdl")
    public interface CalculatorWs {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    }

## ejb-jar.xml

    <ejb-jar/>

## openejb-jar.xml

    <openejb-jar xmlns="http://tomee.apache.org/xml/ns/openejb-jar-2.2">
      <enterprise-beans>
        <session>
          <ejb-name>CalculatorImpl</ejb-name>
          <web-service-security>
            <security-realm-name/>
            <transport-guarantee>NONE</transport-guarantee>
            <auth-method>BASIC</auth-method>
          </web-service-security>
        </session>
      </enterprise-beans>
    </openejb-jar>

## CalculatorTest

    package org.superbiz.calculator;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.BindingProvider;
    import javax.xml.ws.Service;
    import java.net.URL;
    import java.util.Properties;
    
    public class CalculatorTest extends TestCase {
    
        //START SNIPPET: setup
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
        }
        //END SNIPPET: setup
    
        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        //START SNIPPET: webservice
        public void testCalculatorViaWsInterface() throws Exception {
            URL url = new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl");
            QName calcServiceQName = new QName("http://superbiz.org/wsdl", "CalculatorWsService");
            Service calcService = Service.create(url, calcServiceQName);
            assertNotNull(calcService);
    
            CalculatorWs calc = calcService.getPort(CalculatorWs.class);
            ((BindingProvider) calc).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jane");
            ((BindingProvider) calc).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "waterfall");
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }
        //END SNIPPET: webservice
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.calculator.CalculatorTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/webservice-security
    INFO - openejb.base = /Users/dblevins/examples/webservice-security
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/webservice-security/target/classes
    INFO - Beginning load: /Users/dblevins/examples/webservice-security/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/webservice-security/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/webservice-security/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/webservice-security/classpath.ear
    INFO - Jndi(name=CalculatorImplRemote) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/webservice-security/CalculatorImpl!org.superbiz.calculator.CalculatorRemote) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/webservice-security/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/webservice-security/classpath.ear)
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
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.481 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
