index-group=Session Beans
type=page
status=published
title=Simple Stateless with Descriptor
~~~~~~

This test is similar to simple-stateless, with two major differences. In this case all the classes are regular POJOs without annotations.
The EJB-specific metadata is provided via an XML descriptor. The second difference is the explicite use of Local and Remote interfaces. 

## CalculatorImpl

    package org.superbiz.calculator;
    
    /**
     * This is an EJB 3 stateless session bean, configured using an EJB 3
     * deployment descriptor as opposed to using annotations.
     * This EJB has 2 business interfaces: CalculatorRemote, a remote business
     * interface, and CalculatorLocal, a local business interface
     */
    public class CalculatorImpl implements CalculatorRemote, CalculatorLocal {
    
        public int sum(int add1, int add2) {
            return add1 + add2;
        }
    
        public int multiply(int mul1, int mul2) {
            return mul1 * mul2;
        }
    }

## CalculatorLocal

    package org.superbiz.calculator;
    
    /**
     * This is an EJB 3 local business interface
     * This interface is specified using the business-local tag in the deployment descriptor
     */
    public interface CalculatorLocal {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    }

## CalculatorRemote

    package org.superbiz.calculator;
    
    
    /**
     * This is an EJB 3 remote business interface
     * This interface is specified using the business-local tag in the deployment descriptor
     */
    public interface CalculatorRemote {
    
        public int sum(int add1, int add2);
    
        public int multiply(int mul1, int mul2);
    }

## ejb-jar.xml

The XML descriptor defines the EJB class and both local and remote interfaces.

    <ejb-jar xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"
             version="3.0">
      <enterprise-beans>
        <session>
          <ejb-name>CalculatorImpl</ejb-name>
          <business-local>org.superbiz.calculator.CalculatorLocal</business-local>
          <business-remote>org.superbiz.calculator.CalculatorRemote</business-remote>
          <ejb-class>org.superbiz.calculator.CalculatorImpl</ejb-class>
          <session-type>Stateless</session-type>
          <transaction-type>Container</transaction-type>
        </session>
      </enterprise-beans>
    </ejb-jar>

    

## CalculatorTest

Two tests obtain a Local and Remote interface to the bean instance. This time an `InitialContext` object is directly created, 
as opposed to getting the context from `EJBContainer`, as we did in the previous example. 

    package org.superbiz.calculator;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    public class CalculatorTest extends TestCase {
    
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
    
            initialContext = new InitialContext(properties);
        }

        /**
         * Lookup the Calculator bean via its remote home interface
         *
         * @throws Exception
         */
        public void testCalculatorViaRemoteInterface() throws Exception {
            Object object = initialContext.lookup("CalculatorImplRemote");
    
            assertNotNull(object);
            assertTrue(object instanceof CalculatorRemote);
            CalculatorRemote calc = (CalculatorRemote) object;
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }

        /**
         * Lookup the Calculator bean via its local home interface
         *
         * @throws Exception
         */
        public void testCalculatorViaLocalInterface() throws Exception {
            Object object = initialContext.lookup("CalculatorImplLocal");
    
            assertNotNull(object);
            assertTrue(object instanceof CalculatorLocal);
            CalculatorLocal calc = (CalculatorLocal) object;
            assertEquals(10, calc.sum(4, 6));
            assertEquals(12, calc.multiply(3, 4));
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.calculator.CalculatorTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/simple-stateless-with-descriptor
    INFO - openejb.base = /Users/dblevins/examples/simple-stateless-with-descriptor
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-stateless-with-descriptor/target/classes
    INFO - Beginning load: /Users/dblevins/examples/simple-stateless-with-descriptor/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean CalculatorImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear
    INFO - Jndi(name=CalculatorImplLocal) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl!org.superbiz.calculator.CalculatorLocal) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=CalculatorImplRemote) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl!org.superbiz.calculator.CalculatorRemote) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Jndi(name=global/classpath.ear/simple-stateless-with-descriptor/CalculatorImpl) --> Ejb(deployment-id=CalculatorImpl)
    INFO - Created Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=CalculatorImpl, ejb-name=CalculatorImpl, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/simple-stateless-with-descriptor/classpath.ear)
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.475 sec
    
    Results :
    
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
    
