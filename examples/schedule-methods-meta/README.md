index-group=Meta-Annotations
type=page
status=published
title=Schedule Methods Meta
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## BiAnnually

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface BiAnnually {
        public static interface $ {
    
            @BiAnnually
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1", month = "1,6")
            public void method();
        }
    }

## BiMonthly

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface BiMonthly {
        public static interface $ {
    
            @BiMonthly
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "1,15")
            public void method();
        }
    }

## Daily

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Daily {
        public static interface $ {
    
            @Daily
            @Schedule(second = "0", minute = "0", hour = "0", dayOfMonth = "*")
            public void method();
        }
    }

## HarvestTime

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface HarvestTime {
        public static interface $ {
    
            @HarvestTime
            @Schedules({
                    @Schedule(month = "9", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                    @Schedule(month = "10", dayOfMonth = "1-10", minute = "0", hour = "8")
            })
            public void method();
        }
    }

## Hourly

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Hourly {
        public static interface $ {
    
            @Hourly
            @Schedule(second = "0", minute = "0", hour = "*")
            public void method();
        }
    }

## Metatype

    package org.superbiz.corn.meta.api;
    
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metatype {
    }

## Organic

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.Singleton;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    
    @Singleton
    @Lock(LockType.READ)
    public @interface Organic {
    }

## PlantingTime

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import javax.ejb.Schedules;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface PlantingTime {
        public static interface $ {
    
            @PlantingTime
            @Schedules({
                    @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                    @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
            })
            public void method();
        }
    }

## Secondly

    package org.superbiz.corn.meta.api;
    
    import javax.ejb.Schedule;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
    
    @Metatype
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    
    public @interface Secondly {
        public static interface $ {
    
            @Secondly
            @Schedule(second = "*", minute = "*", hour = "*")
            public void method();
        }
    }

## FarmerBrown

    package org.superbiz.corn.meta;
    
    import org.superbiz.corn.meta.api.HarvestTime;
    import org.superbiz.corn.meta.api.Organic;
    import org.superbiz.corn.meta.api.PlantingTime;
    import org.superbiz.corn.meta.api.Secondly;
    
    import java.util.concurrent.atomic.AtomicInteger;
    
    /**
     * This is where we schedule all of Farmer Brown's corn jobs
     *
     * @version $Revision$ $Date$
     */
    @Organic
    public class FarmerBrown {
    
        private final AtomicInteger checks = new AtomicInteger();
    
        @PlantingTime
        private void plantTheCorn() {
            // Dig out the planter!!!
        }
    
        @HarvestTime
        private void harvestTheCorn() {
            // Dig out the combine!!!
        }
    
        @Secondly
        private void checkOnTheDaughters() {
            checks.incrementAndGet();
        }
    
        public int getChecks() {
            return checks.get();
        }
    }

## FarmerBrownTest

    package org.superbiz.corn.meta;
    
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
    
            final FarmerBrown farmerBrown = (FarmerBrown) context.lookup("java:global/schedule-methods-meta/FarmerBrown");
    
            // Give Farmer brown a chance to do some work
            Thread.sleep(SECONDS.toMillis(5));
    
            assertTrue(farmerBrown.getChecks() > 4);
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.corn.meta.FarmerBrownTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/schedule-methods-meta
    INFO - openejb.base = /Users/dblevins/examples/schedule-methods-meta
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/schedule-methods-meta/target/classes
    INFO - Beginning load: /Users/dblevins/examples/schedule-methods-meta/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/schedule-methods-meta
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean FarmerBrown: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.corn.meta.FarmerBrownTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/schedule-methods-meta" loaded.
    INFO - Assembling app: /Users/dblevins/examples/schedule-methods-meta
    INFO - Jndi(name="java:global/schedule-methods-meta/FarmerBrown!org.superbiz.corn.meta.FarmerBrown")
    INFO - Jndi(name="java:global/schedule-methods-meta/FarmerBrown")
    INFO - Jndi(name="java:global/EjbModule1809441479/org.superbiz.corn.meta.FarmerBrownTest!org.superbiz.corn.meta.FarmerBrownTest")
    INFO - Jndi(name="java:global/EjbModule1809441479/org.superbiz.corn.meta.FarmerBrownTest")
    INFO - Created Ejb(deployment-id=org.superbiz.corn.meta.FarmerBrownTest, ejb-name=org.superbiz.corn.meta.FarmerBrownTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.corn.meta.FarmerBrownTest, ejb-name=org.superbiz.corn.meta.FarmerBrownTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=FarmerBrown, ejb-name=FarmerBrown, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/schedule-methods-meta)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.166 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
