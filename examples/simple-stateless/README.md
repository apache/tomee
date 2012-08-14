Title: Simple Stateless

>"Stateless session beans are session beans whose instances have no conversational state.
This means that all bean instances are equivalent when they are not involved in servicing
a client-invoked method. The term 'stateless' signifies that an instance has no state for a
specific client."

What this means is quite simply that stateless beans are shared. They do in fact have state
as you can assign values to the variables, etc. in the bean instance. The only catch is there
are a pool of identical instances and you are not guaranteed to get the exact same instance on
every call. For each call, you get whatever instance happens to be available. This is identical
to checking out a book from the library or renting a movie from the video store. You are essentially
checking out or renting a new bean instance on each method call.

## CalculatorBean

    package org.superbiz.stateless.basic;
    
    import javax.ejb.Stateless;
    
    @Stateless
    public class CalculatorBean {
    
        public int add(int a, int b) {
            return a + b;
        }
    
        public int subtract(int a, int b) {
            return a - b;
        }
    
        public int multiply(int a, int b) {
            return a * b;
        }
    
        public int divide(int a, int b) {
            return a / b;
        }
    
        public int remainder(int a, int b) {
            return a % b;
        }
    }

## CalculatorTest

Our `CalculatorBean` can be easily tested using the `EJBContainer` API in EJB 3.1

    package org.superbiz.stateless.basic;

    import org.junit.AfterClass;
    import org.junit.Before;
    import org.junit.BeforeClass;
    import org.junit.Test;

    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.NamingException;

    import static org.junit.Assert.assertEquals;
    import static org.junit.Assert.assertTrue;

    public class CalculatorTest {

        private static EJBContainer ejbContainer;

        private CalculatorBean calculator;

        @BeforeClass
        public static void startTheContainer() {
            ejbContainer = EJBContainer.createEJBContainer();
        }

        @Before
        public void lookupABean() throws NamingException {
            Object object = ejbContainer.getContext().lookup("java:global/simple-stateless/CalculatorBean");

            assertTrue(object instanceof CalculatorBean);

            calculator = (CalculatorBean) object;
        }

        @AfterClass
        public static void stopTheContainer() {
            if (ejbContainer != null) {
                ejbContainer.close();
            }
        }

        /**
         * Test Add method
         */
        @Test
        public void testAdd() {

            assertEquals(10, calculator.add(4, 6));

        }

        /**
         * Test Subtract method
         */
        @Test
        public void testSubtract() {

            assertEquals(-2, calculator.subtract(4, 6));

        }

        /**
         * Test Multiply method
         */
        @Test
        public void testMultiply() {

            assertEquals(24, calculator.multiply(4, 6));

        }

        /**
         * Test Divide method
         */
        @Test
        public void testDivide() {

            assertEquals(2, calculator.divide(12, 6));

        }

        /**
         * Test Remainder method
         */
        @Test
        public void testRemainder() {

            assertEquals(4, calculator.remainder(46, 6));

        }

    }

# Running


Running the example should generate output similar to the following

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.stateless.basic.CalculatorTest
    Infos - ********************************************************************************
    Infos - OpenEJB http://openejb.apache.org/
    Infos - Startup: Tue Aug 14 13:28:12 CEST 2012
    Infos - Copyright 1999-2012 (C) Apache OpenEJB Project, All Rights Reserved.
    Infos - Version: 4.1.0-SNAPSHOT
    Infos - Build date: 20120814
    Infos - Build time: 01:06
    Infos - ********************************************************************************
    Infos - openejb.home = /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless
    Infos - openejb.base = /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless
    Infos - Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@33bb11
    Infos - Succeeded in installing singleton service
    Infos - Using 'javax.ejb.embeddable.EJBContainer=true'
    Infos - Cannot find the configuration file [conf/openejb.xml].  Will attempt to create one for the beans deployed.
    Infos - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    Infos - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    Infos - Creating TransactionManager(id=Default Transaction Manager)
    Infos - Creating SecurityService(id=Default Security Service)
    Infos - Beginning load: /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless/target/classes
    Infos - Configuring enterprise application: /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless
    Infos - Auto-deploying ejb CalculatorBean: EjbDeployment(deployment-id=CalculatorBean)
    Infos - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    Infos - Auto-creating a container for bean CalculatorBean: Container(type=STATELESS, id=Default Stateless Container)
    Infos - Creating Container(id=Default Stateless Container)
    Infos - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    Infos - Auto-creating a container for bean org.superbiz.stateless.basic.CalculatorTest: Container(type=MANAGED, id=Default Managed Container)
    Infos - Creating Container(id=Default Managed Container)
    Infos - Using directory /tmp for stateful session passivation
    Infos - Enterprise application "/home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless" loaded.
    Infos - Assembling app: /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless
    Infos - Jndi(name="java:global/simple-stateless/CalculatorBean!org.superbiz.stateless.basic.CalculatorBean")
    Infos - Jndi(name="java:global/simple-stateless/CalculatorBean")
    Infos - Existing thread singleton service in SystemInstance() org.apache.openejb.cdi.ThreadSingletonServiceImpl@33bb11
    Infos - OpenWebBeans Container is starting...
    Infos - Adding OpenWebBeansPlugin : [CdiPlugin]
    Infos - All injection points are validated successfully.
    Infos - OpenWebBeans Container has started, it took 135 ms.
    Infos - Created Ejb(deployment-id=CalculatorBean, ejb-name=CalculatorBean, container=Default Stateless Container)
    Infos - Started Ejb(deployment-id=CalculatorBean, ejb-name=CalculatorBean, container=Default Stateless Container)
    Infos - Deployed Application(path=/home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless)
    Infos - Undeploying app: /home/a185558/Development/Apache/openejb-trunk/examples/simple-stateless
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.068 sec
    
    Results :
    
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
    
