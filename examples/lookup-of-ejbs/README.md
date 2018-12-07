index-group=Referencing EJBs
type=page
status=published
title=Lookup Of Ejbs
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## BlueBean

    package org.superbiz.ejblookup;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBException;
    import javax.ejb.Stateless;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    
    //START SNIPPET: code
    @Stateless
    @EJB(beanInterface = Friend.class, beanName = "RedBean", name = "myFriend")
    public class BlueBean implements Friend {
    
        public String sayHello() {
            return "Blue says, Hello!";
        }
    
        public String helloFromFriend() {
            try {
                Friend friend = (Friend) new InitialContext().lookup("java:comp/env/myFriend");
                return "My friend " + friend.sayHello();
            } catch (NamingException e) {
                throw new EJBException(e);
            }
        }
    }

## Friend

    package org.superbiz.ejblookup;
    
    import javax.ejb.Local;
    
    /**
     * This is an EJB 3 local business interface
     * A local business interface may be annotated with the @Local
     * annotation, but it's optional. A business interface which is
     * not annotated with @Local or @Remote is assumed to be Local
     * if the bean does not implement any other interfaces
     */
    //START SNIPPET: code
    @Local
    public interface Friend {
    
        public String sayHello();
    
        public String helloFromFriend();
    }

## RedBean

    package org.superbiz.ejblookup;
    
    import javax.ejb.EJB;
    import javax.ejb.EJBException;
    import javax.ejb.Stateless;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    
    //START SNIPPET: code
    @Stateless
    @EJB(beanInterface = Friend.class, beanName = "BlueBean", name = "myFriend")
    public class RedBean implements Friend {
    
        public String sayHello() {
            return "Red says, Hello!";
        }
    
        public String helloFromFriend() {
            try {
                Friend friend = (Friend) new InitialContext().lookup("java:comp/env/myFriend");
                return "My friend " + friend.sayHello();
            } catch (NamingException e) {
                throw new EJBException(e);
            }
        }
    }

## EjbDependencyTest

    package org.superbiz.ejblookup;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    //START SNIPPET: code
    public class EjbDependencyTest extends TestCase {
    
        private Context context;
    
        protected void setUp() throws Exception {
            context = EJBContainer.createEJBContainer().getContext();
        }
    
        public void testRed() throws Exception {
    
            Friend red = (Friend) context.lookup("java:global/lookup-of-ejbs/RedBean");
    
            assertNotNull(red);
            assertEquals("Red says, Hello!", red.sayHello());
            assertEquals("My friend Blue says, Hello!", red.helloFromFriend());
        }
    
        public void testBlue() throws Exception {
    
            Friend blue = (Friend) context.lookup("java:global/lookup-of-ejbs/BlueBean");
    
            assertNotNull(blue);
            assertEquals("Blue says, Hello!", blue.sayHello());
            assertEquals("My friend Red says, Hello!", blue.helloFromFriend());
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.ejblookup.EjbDependencyTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/lookup-of-ejbs
    INFO - openejb.base = /Users/dblevins/examples/lookup-of-ejbs
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/lookup-of-ejbs/target/classes
    INFO - Beginning load: /Users/dblevins/examples/lookup-of-ejbs/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/lookup-of-ejbs
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean RedBean: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.ejblookup.EjbDependencyTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/lookup-of-ejbs" loaded.
    INFO - Assembling app: /Users/dblevins/examples/lookup-of-ejbs
    INFO - Jndi(name="java:global/lookup-of-ejbs/RedBean!org.superbiz.ejblookup.Friend")
    INFO - Jndi(name="java:global/lookup-of-ejbs/RedBean")
    INFO - Jndi(name="java:global/lookup-of-ejbs/BlueBean!org.superbiz.ejblookup.Friend")
    INFO - Jndi(name="java:global/lookup-of-ejbs/BlueBean")
    INFO - Jndi(name="java:global/EjbModule1374821456/org.superbiz.ejblookup.EjbDependencyTest!org.superbiz.ejblookup.EjbDependencyTest")
    INFO - Jndi(name="java:global/EjbModule1374821456/org.superbiz.ejblookup.EjbDependencyTest")
    INFO - Created Ejb(deployment-id=RedBean, ejb-name=RedBean, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=BlueBean, ejb-name=BlueBean, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=org.superbiz.ejblookup.EjbDependencyTest, ejb-name=org.superbiz.ejblookup.EjbDependencyTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=RedBean, ejb-name=RedBean, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=BlueBean, ejb-name=BlueBean, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=org.superbiz.ejblookup.EjbDependencyTest, ejb-name=org.superbiz.ejblookup.EjbDependencyTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/lookup-of-ejbs)
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.267 sec
    
    Results :
    
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
    
