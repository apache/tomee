index-group=Unrevised
type=page
status=published
title=Helloworld Weblogic
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## HelloBean

    package org.superbiz.hello;
    
    import javax.ejb.LocalHome;
    import javax.ejb.Stateless;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    @Stateless
    @LocalHome(HelloEjbLocalHome.class)
    public class HelloBean {
    
        public String sayHello() {
            return "Hello, World!";
        }
    }

## HelloEjbLocal

    package org.superbiz.hello;
    
    import javax.ejb.EJBLocalObject;
    
    /**
     * @version $Revision: 607077 $ $Date: 2007-12-27 06:55:23 -0800 (Thu, 27 Dec 2007) $
     */
    public interface HelloEjbLocal extends EJBLocalObject {
    
        String sayHello();
    }

## HelloEjbLocalHome

    package org.superbiz.hello;
    
    import javax.ejb.CreateException;
    import javax.ejb.EJBLocalHome;
    
    /**
     * @version $Revision: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public interface HelloEjbLocalHome extends EJBLocalHome {
        HelloEjbLocal create() throws CreateException;
    }

## ejb-jar.xml

    <ejb-jar/>

## weblogic-ejb-jar.xml

    <weblogic-ejb-jar>
      <weblogic-enterprise-bean>
        <ejb-name>HelloBean</ejb-name>
        <local-jndi-name>MyHello</local-jndi-name>
      </weblogic-enterprise-bean>
    </weblogic-ejb-jar>
    
    

## HelloTest

    package org.superbiz.hello;
    
    import junit.framework.TestCase;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;
    
    /**
     * @version $Revision: 1090810 $ $Date: 2011-04-10 07:49:26 -0700 (Sun, 10 Apr 2011) $
     */
    public class HelloTest extends TestCase {
    
        public void test() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            InitialContext initialContext = new InitialContext(properties);
    
            HelloEjbLocalHome localHome = (HelloEjbLocalHome) initialContext.lookup("MyHello");
            HelloEjbLocal helloEjb = localHome.create();
    
            String message = helloEjb.sayHello();
    
            assertEquals(message, "Hello, World!");
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.hello.HelloTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/helloworld-weblogic
    INFO - openejb.base = /Users/dblevins/examples/helloworld-weblogic
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/helloworld-weblogic/target/classes
    INFO - Beginning load: /Users/dblevins/examples/helloworld-weblogic/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/helloworld-weblogic/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean HelloBean: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/helloworld-weblogic/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/helloworld-weblogic/classpath.ear
    INFO - Jndi(name=MyHello) --> Ejb(deployment-id=HelloBean)
    INFO - Jndi(name=global/classpath.ear/helloworld-weblogic/HelloBean!org.superbiz.hello.HelloEjbLocalHome) --> Ejb(deployment-id=HelloBean)
    INFO - Jndi(name=global/classpath.ear/helloworld-weblogic/HelloBean) --> Ejb(deployment-id=HelloBean)
    INFO - Created Ejb(deployment-id=HelloBean, ejb-name=HelloBean, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=HelloBean, ejb-name=HelloBean, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/helloworld-weblogic/classpath.ear)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.414 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
