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
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    
    public class CalculatorTest extends TestCase {
    
        private CalculatorBean calculator;
    
        /**
         * Bootstrap the Embedded EJB Container
         *
         * @throws Exception
         */
        protected void setUp() throws Exception {
    
            EJBContainer ejbContainer = EJBContainer.createEJBContainer();
    
            Object object = ejbContainer.getContext().lookup("java:global/simple-stateless/CalculatorBean");
    
            assertTrue(object instanceof CalculatorBean);
    
            calculator = (CalculatorBean) object;
        }
    
        /**
         * Test Add method
         */
        public void testAdd() {
    
            assertEquals(10, calculator.add(4, 6));
        }
    
        /**
         * Test Subtract method
         */
        public void testSubtract() {
    
            assertEquals(-2, calculator.subtract(4, 6));
        }
    
        /**
         * Test Multiply method
         */
        public void testMultiply() {
    
            assertEquals(24, calculator.multiply(4, 6));
        }
    
        /**
         * Test Divide method
         */
        public void testDivide() {
    
            assertEquals(2, calculator.divide(12, 6));
        }
    
        /**
         * Test Remainder method
         */
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
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/simple-stateless
    INFO - openejb.base = /Users/dblevins/examples/simple-stateless
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-stateless/target/classes
    INFO - Beginning load: /Users/dblevins/examples/simple-stateless/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/simple-stateless
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean CalculatorBean: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.stateless.basic.CalculatorTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/simple-stateless" loaded.
    INFO - Assembling app: /Users/dblevins/examples/simple-stateless
    INFO - Jndi(name="java:global/simple-stateless/CalculatorBean!org.superbiz.stateless.basic.CalculatorBean")
    INFO - Jndi(name="java:global/simple-stateless/CalculatorBean")
    INFO - Jndi(name="java:global/EjbModule181871104/org.superbiz.stateless.basic.CalculatorTest!org.superbiz.stateless.basic.CalculatorTest")
    INFO - Jndi(name="java:global/EjbModule181871104/org.superbiz.stateless.basic.CalculatorTest")
    INFO - Created Ejb(deployment-id=CalculatorBean, ejb-name=CalculatorBean, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=org.superbiz.stateless.basic.CalculatorTest, ejb-name=org.superbiz.stateless.basic.CalculatorTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=CalculatorBean, ejb-name=CalculatorBean, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=org.superbiz.stateless.basic.CalculatorTest, ejb-name=org.superbiz.stateless.basic.CalculatorTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/simple-stateless)
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.068 sec
    
    Results :
    
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
    
