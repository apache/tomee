index-group=Unrevised
type=page
status=published
title=Schedule Methods
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## FarmerBrown

    package org.superbiz.corn;
    
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import javax.ejb.Singleton;
    import java.util.concurrent.atomic.AtomicInteger;
    
    /**
     * This is where we schedule all of Farmer Brown's corn jobs
     *
     * @version $Revision$ $Date$
     */
    @Singleton
    @Lock(LockType.READ) // allows timers to execute in parallel
    public class FarmerBrown {
    
        private final AtomicInteger checks = new AtomicInteger();
    
        @Schedules({
                @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
        })
        private void plantTheCorn() {
            // Dig out the planter!!!
        }
    
        @Schedules({
                @Schedule(month = "9", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                @Schedule(month = "10", dayOfMonth = "1-10", minute = "0", hour = "8")
        })
        private void harvestTheCorn() {
            // Dig out the combine!!!
        }
    
        @Schedule(second = "*", minute = "*", hour = "*")
        private void checkOnTheDaughters() {
            checks.incrementAndGet();
        }
    
        public int getChecks() {
            return checks.get();
        }
    }

## FarmerBrownTest

    package org.superbiz.corn;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    import static java.util.concurrent.TimeUnit.SECONDS;
    
    /**
     * @version $Revision$ $Date$
     */
    public class FarmerBrownTest extends TestCase {
    
        public void test() throws Exception {
    
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            final FarmerBrown farmerBrown = (FarmerBrown) context.lookup("java:global/schedule-methods/FarmerBrown");
    
            // Give Farmer brown a chance to do some work
            Thread.sleep(SECONDS.toMillis(5));
    
            assertTrue(farmerBrown.getChecks() > 4);
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.corn.FarmerBrownTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/schedule-methods
    INFO - openejb.base = /Users/dblevins/examples/schedule-methods
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/schedule-methods/target/classes
    INFO - Beginning load: /Users/dblevins/examples/schedule-methods/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/schedule-methods
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean FarmerBrown: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.corn.FarmerBrownTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/schedule-methods" loaded.
    INFO - Assembling app: /Users/dblevins/examples/schedule-methods
    INFO - Jndi(name="java:global/schedule-methods/FarmerBrown!org.superbiz.corn.FarmerBrown")
    INFO - Jndi(name="java:global/schedule-methods/FarmerBrown")
    INFO - Jndi(name="java:global/EjbModule660493198/org.superbiz.corn.FarmerBrownTest!org.superbiz.corn.FarmerBrownTest")
    INFO - Jndi(name="java:global/EjbModule660493198/org.superbiz.corn.FarmerBrownTest")
    INFO - Created Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.corn.FarmerBrownTest, ejb-name=org.superbiz.corn.FarmerBrownTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/schedule-methods)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.121 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
