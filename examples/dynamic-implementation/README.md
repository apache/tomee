index-group=Unrevised
type=page
status=published
title=Dynamic Implementation
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## SocialBean

    package org.superbiz.dynamic;
    
    import org.apache.openejb.api.Proxy;
    
    import javax.ejb.Singleton;
    import javax.interceptor.Interceptors;
    
    @Singleton
    @Proxy(SocialHandler.class)
    @Interceptors(SocialInterceptor.class)
    public interface SocialBean {
        public String facebookStatus();
    
        public String twitterStatus();
    
        public String status();
    }

## SocialHandler

    package org.superbiz.dynamic;
    
    import java.lang.reflect.InvocationHandler;
    import java.lang.reflect.Method;
    
    public class SocialHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String mtd = method.getName();
            if (mtd.toLowerCase().contains("facebook")) {
                return "You think you have a life!";
            } else if (mtd.toLowerCase().contains("twitter")) {
                return "Wow, you eat pop corn!";
            }
            return "Hey, you have no virtual friend!";
        }
    }

## SocialInterceptor

    packagenull
    }

## SocialTest

    package org.superbiz.dynamic;
    
    import org.junit.AfterClass;
    import org.junit.BeforeClass;
    import org.junit.Test;
    
    import javax.ejb.embeddable.EJBContainer;
    
    import static junit.framework.Assert.assertTrue;
    
    public class SocialTest {
        private static SocialBean social;
        private static EJBContainer container;
    
        @BeforeClass
        public static void init() throws Exception {
            container = EJBContainer.createEJBContainer();
            social = (SocialBean) container.getContext().lookup("java:global/dynamic-implementation/SocialBean");
        }
    
        @AfterClass
        public static void close() {
            container.close();
        }
    
        @Test
        public void simple() {
            assertTrue(social.facebookStatus().contains("think"));
            assertTrue(social.twitterStatus().contains("eat"));
            assertTrue(social.status().contains("virtual"));
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.dynamic.SocialTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/dynamic-implementation
    INFO - openejb.base = /Users/dblevins/examples/dynamic-implementation
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/dynamic-implementation/target/classes
    INFO - Beginning load: /Users/dblevins/examples/dynamic-implementation/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/dynamic-implementation
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean SocialBean: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.dynamic.SocialTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/dynamic-implementation" loaded.
    INFO - Assembling app: /Users/dblevins/examples/dynamic-implementation
    INFO - Jndi(name="java:global/dynamic-implementation/SocialBean!org.superbiz.dynamic.SocialBean")
    INFO - Jndi(name="java:global/dynamic-implementation/SocialBean")
    INFO - Jndi(name="java:global/EjbModule236706648/org.superbiz.dynamic.SocialTest!org.superbiz.dynamic.SocialTest")
    INFO - Jndi(name="java:global/EjbModule236706648/org.superbiz.dynamic.SocialTest")
    INFO - Created Ejb(deployment-id=org.superbiz.dynamic.SocialTest, ejb-name=org.superbiz.dynamic.SocialTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=SocialBean, ejb-name=SocialBean, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.dynamic.SocialTest, ejb-name=org.superbiz.dynamic.SocialTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=SocialBean, ejb-name=SocialBean, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/dynamic-implementation)
    INFO - Undeploying app: /Users/dblevins/examples/dynamic-implementation
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.107 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
