Title: CDI Interceptors

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/cdi-interceptors) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/cdi-interceptors). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## AccessDeniedException

    package org.superbiz.cdi;
    
    import javax.ejb.ApplicationException;
    
    /**
     * @version $Revision$ $Date$
     */
    @ApplicationException
    public class AccessDeniedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    
        public AccessDeniedException(String s) {
            super(s);
        }
    }

## BookForAShowOldStyleInterceptorBinding

    package org.superbiz.cdi.bookshow.beans;
    
    import org.superbiz.cdi.bookshow.interceptorbinding.Log;
    import org.superbiz.cdi.bookshow.interceptors.BookForAShowLoggingInterceptor;
    
    import javax.ejb.Stateful;
    import javax.interceptor.Interceptors;
    import java.io.Serializable;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * CDI supports binding an interceptor using @Interceptors
     * Not recommended though. Has its disadvantages 
     * Cannot be disabled easily
     * Order dependent on how it is listed in class
     * Instead, create interceptor bindings using @InterceptorBinding and bind them 
     * See {@link Log}, {@link BookForAShowOneInterceptorApplied}, {@link BookForAShowLoggingInterceptor}
     */
    @Interceptors(BookForAShowLoggingInterceptor.class)
    @Stateful
    public class BookForAShowOldStyleInterceptorBinding implements Serializable {
        private static final long serialVersionUID = 6350400892234496909L;
    
        public List<String> getMoviesList() {
            List<String> moviesAvailable = new ArrayList<String>();
            moviesAvailable.add("KungFu Panda 2");
            moviesAvailable.add("Kings speech");
            return moviesAvailable;
        }
    
        public Integer getDiscountedPrice(int ticketPrice) {
            return ticketPrice - 50;
        }
        // assume more methods are present
    }

## BookForAShowOneInterceptorApplied

    package org.superbiz.cdi.bookshow.beans;
    
    import org.superbiz.cdi.bookshow.interceptorbinding.Log;
    
    import javax.ejb.Stateful;
    import java.io.Serializable;
    import java.util.ArrayList;
    import java.util.List;
    
    @Log
    @Stateful
    public class BookForAShowOneInterceptorApplied implements Serializable {
        private static final long serialVersionUID = 6350400892234496909L;
    
        public List<String> getMoviesList() {
            List<String> moviesAvailable = new ArrayList<String>();
            moviesAvailable.add("12 Angry Men");
            moviesAvailable.add("Kings speech");
            return moviesAvailable;
        }
    
        public Integer getDiscountedPrice(int ticketPrice) {
            return ticketPrice - 50;
        }
        // assume more methods are present
    }

## BookForAShowTwoInterceptorsApplied

    package org.superbiz.cdi.bookshow.beans;
    
    import org.superbiz.cdi.bookshow.interceptorbinding.Log;
    import org.superbiz.cdi.bookshow.interceptorbinding.TimeRestricted;
    
    import javax.ejb.Stateful;
    import java.io.Serializable;
    import java.util.ArrayList;
    import java.util.List;
    
    @Log
    @Stateful
    public class BookForAShowTwoInterceptorsApplied implements Serializable {
        private static final long serialVersionUID = 6350400892234496909L;
    
        public List<String> getMoviesList() {
            List<String> moviesAvailable = new ArrayList<String>();
            moviesAvailable.add("12 Angry Men");
            moviesAvailable.add("Kings speech");
            return moviesAvailable;
        }
    
        @TimeRestricted
        public Integer getDiscountedPrice(int ticketPrice) {
            return ticketPrice - 50;
        }
        // assume more methods are present
    }

## BookShowInterceptorBindingInheritanceExplored

    package org.superbiz.cdi.bookshow.beans;
    
    import org.superbiz.cdi.bookshow.interceptorbinding.TimeRestrictAndLog;
    
    import javax.ejb.Stateful;
    import java.io.Serializable;
    import java.util.ArrayList;
    import java.util.List;
    
    @Stateful
    public class BookShowInterceptorBindingInheritanceExplored implements Serializable {
        private static final long serialVersionUID = 6350400892234496909L;
    
        public List<String> getMoviesList() {
            List<String> moviesAvailable = new ArrayList<String>();
            moviesAvailable.add("12 Angry Men");
            moviesAvailable.add("Kings speech");
            return moviesAvailable;
        }
    
        @TimeRestrictAndLog
        public Integer getDiscountedPrice(int ticketPrice) {
            return ticketPrice - 50;
        }
        // assume more methods are present
    }

## Log

    package org.superbiz.cdi.bookshow.interceptorbinding;
    
    import javax.interceptor.InterceptorBinding;
    import java.lang.annotation.Retention;
    import java.lang.annotation.Target;
    
    import static java.lang.annotation.ElementType.METHOD;
    import static java.lang.annotation.ElementType.TYPE;
    import static java.lang.annotation.RetentionPolicy.RUNTIME;
    
    @InterceptorBinding
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    public @interface Log {
    }

## TimeRestrictAndLog

    package org.superbiz.cdi.bookshow.interceptorbinding;
    
    import javax.interceptor.InterceptorBinding;
    import java.lang.annotation.Inherited;
    import java.lang.annotation.Retention;
    import java.lang.annotation.Target;
    
    import static java.lang.annotation.ElementType.METHOD;
    import static java.lang.annotation.ElementType.TYPE;
    import static java.lang.annotation.RetentionPolicy.RUNTIME;
    
    /**
     *This InterceptorBinding inherits from @Log and @TimeRestricted Interceptor-Bindings.
     */
    @Inherited
    @InterceptorBinding
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    @Log
    @TimeRestricted
    public @interface TimeRestrictAndLog {
    }

## TimeRestricted

    package org.superbiz.cdi.bookshow.interceptorbinding;
    
    import javax.interceptor.InterceptorBinding;
    import java.lang.annotation.Retention;
    import java.lang.annotation.Target;
    
    import static java.lang.annotation.ElementType.METHOD;
    import static java.lang.annotation.ElementType.TYPE;
    import static java.lang.annotation.RetentionPolicy.RUNTIME;
    
    @InterceptorBinding
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    public @interface TimeRestricted {
    }

## BookForAShowLoggingInterceptor

    package org.superbiz.cdi.bookshow.interceptors;
    
    import org.superbiz.cdi.bookshow.interceptorbinding.Log;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.Interceptor;
    import javax.interceptor.InvocationContext;
    import java.io.Serializable;
    import java.util.logging.Logger;
    
    @Interceptor
    @Log
    public class BookForAShowLoggingInterceptor implements Serializable {
        private static final long serialVersionUID = 8139854519874743530L;
        private Logger logger = Logger.getLogger("BookForAShowApplicationLogger");
    
        @AroundInvoke
        public Object logMethodEntry(InvocationContext ctx) throws Exception {
            logger.info("Before entering method:" + ctx.getMethod().getName());
            InterceptionOrderTracker.getMethodsInterceptedList().add(ctx.getMethod().getName());
            InterceptionOrderTracker.getInterceptedByList().add(this.getClass().getSimpleName());
            return ctx.proceed();
        }
    }

## TimeBasedRestrictingInterceptor

    package org.superbiz.cdi.bookshow.interceptors;
    
    import org.superbiz.cdi.AccessDeniedException;
    import org.superbiz.cdi.bookshow.interceptorbinding.TimeRestricted;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.interceptor.AroundInvoke;
    import javax.interceptor.Interceptor;
    import javax.interceptor.InvocationContext;
    import java.io.Serializable;
    
    @Interceptor
    @TimeRestricted
    public class TimeBasedRestrictingInterceptor implements Serializable {
        private static final long serialVersionUID = 8139854519874743530L;
    
        @AroundInvoke
        public Object restrictAccessBasedOnTime(InvocationContext ctx) throws Exception {
            InterceptionOrderTracker.getMethodsInterceptedList().add(ctx.getMethod().getName());
            InterceptionOrderTracker.getInterceptedByList().add(this.getClass().getSimpleName());
            if (!isWorkingHours()) {
                throw new AccessDeniedException("You are not allowed to access the method at this time");
            }
            return ctx.proceed();
        }
    
        private boolean isWorkingHours() {
            /*
             * int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); if (hourOfDay >= 9 && hourOfDay <= 21) {
             * return true; } else { return false; }
             */
            return true; // Let's assume
        }
    }

## InterceptionOrderTracker

    package org.superbiz.cdi.bookshow.tracker;
    
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * A helper class for the test. 
     * Keeps track of methods intercepted during one testXXX run
     * Keeps track of interceptors applied during one textXXX run
     */
    public class InterceptionOrderTracker {
        /*
         * Contains method names that were intercepted by the interceptors
         */
        private static List<String> methodsInterceptedList = new ArrayList<String>();
        /*
         * Contains the name of the interceptor class that intercepted a method
         */
        private static List<String> interceptedByList = new ArrayList<String>();
    
        public static List<String> getInterceptedByList() {
            return interceptedByList;
        }
    
        public static void setInterceptedByList(List<String> interceptedByList) {
            InterceptionOrderTracker.interceptedByList = interceptedByList;
        }
    
        public static List<String> getMethodsInterceptedList() {
            return methodsInterceptedList;
        }
    }

## beans.xml

    <beans>
      <!-- By default, a bean archive has no enabled interceptors bound via interceptor 
        bindings. An interceptor must be explicitly enabled by listing its class 
        under the element of the beans.xml file of the bean archive. The order of 
        the interceptor declarations determines the interceptor ordering. Interceptors 
        which occur earlier in the list are called first. If the same class is listed 
        twice under the interceptors element, the container automatically detects 
        the problem and treats it as a deployment problem. -->
    
      <interceptors>
        <class>org.superbiz.cdi.bookshow.interceptors.BookForAShowLoggingInterceptor
        </class>
        <class>org.superbiz.cdi.bookshow.interceptors.TimeBasedRestrictingInterceptor
        </class>
      </interceptors>
    </beans>
    

## BookForAShowOldStyleInterceptorBindingTest

    package org.superbiz.cdi.bookshow.interceptors;
    
    import junit.framework.TestCase;
    import org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    
    public class BookForAShowOldStyleInterceptorBindingTest extends TestCase {
        @EJB
        private BookForAShowOldStyleInterceptorBinding bookForAShowBean;
        EJBContainer ejbContainer;
    
        /**
         * Bootstrap the Embedded EJB Container
         *
         * @throws Exception
         */
        protected void setUp() throws Exception {
            ejbContainer = EJBContainer.createEJBContainer();
            ejbContainer.getContext().bind("inject", this);
        }
    
        /**
         * Test basic interception
         */
        public void testMethodShouldBeIntercepted() {
            // action
            bookForAShowBean.getMoviesList();
            // verify
            assertTrue(InterceptionOrderTracker.getMethodsInterceptedList().contains("getMoviesList"));
        }
    
        protected void tearDown() {
            // clear the lists after each test
            InterceptionOrderTracker.getInterceptedByList().clear();
            InterceptionOrderTracker.getMethodsInterceptedList().clear();
            ejbContainer.close();
        }
    }

## BookForAShowOneInterceptorAppliedTest

    package org.superbiz.cdi.bookshow.interceptors;
    
    import junit.framework.TestCase;
    import org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    
    public class BookForAShowOneInterceptorAppliedTest extends TestCase {
        @EJB
        private BookForAShowOneInterceptorApplied bookForAShowBean;
        EJBContainer ejbContainer;
    
        /**
         * Bootstrap the Embedded EJB Container
         *
         * @throws Exception
         */
        protected void setUp() throws Exception {
            ejbContainer = EJBContainer.createEJBContainer();
            ejbContainer.getContext().bind("inject", this);
        }
    
        /**
         * Test basic interception
         */
        public void testMethodShouldBeIntercepted() {
            // action
            bookForAShowBean.getMoviesList();
            // verify
            assertTrue(InterceptionOrderTracker.getMethodsInterceptedList().contains("getMoviesList"));
        }
    
        protected void tearDown() {
            // clear the list after each test
            InterceptionOrderTracker.getInterceptedByList().clear();
            InterceptionOrderTracker.getMethodsInterceptedList().clear();
            ejbContainer.close();
        }
    }

## BookForAShowTwoInterceptorsAppiledTest

    package org.superbiz.cdi.bookshow.interceptors;
    
    import junit.framework.TestCase;
    import org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import java.util.List;
    
    public class BookForAShowTwoInterceptorsAppiledTest extends TestCase {
        @EJB
        private BookForAShowTwoInterceptorsApplied bookForAShowBean;
        EJBContainer ejbContainer;
    
        /**
         * Bootstrap the Embedded EJB Container
         *
         * @throws Exception
         */
        protected void setUp() throws Exception {
            ejbContainer = EJBContainer.createEJBContainer();
            ejbContainer.getContext().bind("inject", this);
        }
    
        /**
         * Interceptors should be applied in order as defined in beans.xml
         */
        public void testInterceptorsShouldBeAppliedInOrder() {
            // action
            bookForAShowBean.getDiscountedPrice(100);
            // verify
            List<String> interceptedByList = InterceptionOrderTracker.getInterceptedByList();
            int indexOfLogger = interceptedByList.indexOf("BookForAShowLoggingInterceptor");
            int indexOfTimeBasedRestrictor = interceptedByList.indexOf("TimeBasedRestrictingInterceptor");
            assertTrue(indexOfLogger < indexOfTimeBasedRestrictor);
        }
    
        public void testTwoInterceptorsWereInvoked() {
            // action
            bookForAShowBean.getDiscountedPrice(100);
            // verify
            List<String> interceptedByList = InterceptionOrderTracker.getInterceptedByList();
            assertTrue(interceptedByList.contains("BookForAShowLoggingInterceptor") && interceptedByList.contains("TimeBasedRestrictingInterceptor"));
        }
    
        protected void tearDown() {
            // clear the lists after each test
            InterceptionOrderTracker.getInterceptedByList().clear();
            InterceptionOrderTracker.getMethodsInterceptedList().clear();
            ejbContainer.close();
        }
    }

## BookShowInterceptorBindingInheritanceTest

    package org.superbiz.cdi.bookshow.interceptors;
    
    import junit.framework.TestCase;
    import org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored;
    import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;
    
    import javax.ejb.EJB;
    import javax.ejb.embeddable.EJBContainer;
    import java.util.List;
    
    public class BookShowInterceptorBindingInheritanceTest extends TestCase {
        @EJB
        private BookShowInterceptorBindingInheritanceExplored bookForAShowBean;
        EJBContainer ejbContainer;
    
        /**
         * Bootstrap the Embedded EJB Container
         *
         * @throws Exception
         */
        protected void setUp() throws Exception {
            ejbContainer = EJBContainer.createEJBContainer();
            ejbContainer.getContext().bind("inject", this);
        }
    
        public void testInterceptorBindingCanInheritFromAnotherBinding() {
            // action
            bookForAShowBean.getDiscountedPrice(100);
            // verify both interceptors were invoked
            List<String> interceptedByList = InterceptionOrderTracker.getInterceptedByList();
            System.out.println("Intercepted by:" + interceptedByList);
            assertTrue(interceptedByList.contains("BookForAShowLoggingInterceptor") && interceptedByList.contains("TimeBasedRestrictingInterceptor"));
        }
    
        protected void tearDown() {
            // clear the list after each test
            InterceptionOrderTracker.getInterceptedByList().clear();
            InterceptionOrderTracker.getMethodsInterceptedList().clear();
            ejbContainer.close();
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/cdi-interceptors
    INFO - openejb.base = /Users/dblevins/examples/cdi-interceptors
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Beginning load: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-interceptors
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean cdi-interceptors.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean BookShowInterceptorBindingInheritanceExplored: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Enterprise application "/Users/dblevins/examples/cdi-interceptors" loaded.
    INFO - Assembling app: /Users/dblevins/examples/cdi-interceptors
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored!org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied!org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding!org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied!org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/EjbModule103285717/org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest!org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest")
    INFO - Jndi(name="java:global/EjbModule103285717/org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest")
    INFO - Created Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowOldStyleInterceptorBindingTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/cdi-interceptors)
    INFO - Undeploying app: /Users/dblevins/examples/cdi-interceptors
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.334 sec
    Running org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/cdi-interceptors
    INFO - openejb.base = /Users/dblevins/examples/cdi-interceptors
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Beginning load: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-interceptors
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean cdi-interceptors.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean BookForAShowOneInterceptorApplied: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Enterprise application "/Users/dblevins/examples/cdi-interceptors" loaded.
    INFO - Assembling app: /Users/dblevins/examples/cdi-interceptors
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied!org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding!org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored!org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied!org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/EjbModule372775507/org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest!org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest")
    INFO - Jndi(name="java:global/EjbModule372775507/org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest")
    INFO - Created Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowOneInterceptorAppliedTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/cdi-interceptors)
    INFO - Undeploying app: /Users/dblevins/examples/cdi-interceptors
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.263 sec
    Running org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/cdi-interceptors
    INFO - openejb.base = /Users/dblevins/examples/cdi-interceptors
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Beginning load: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-interceptors
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean cdi-interceptors.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean BookForAShowOneInterceptorApplied: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Enterprise application "/Users/dblevins/examples/cdi-interceptors" loaded.
    INFO - Assembling app: /Users/dblevins/examples/cdi-interceptors
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied!org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied!org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding!org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored!org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/EjbModule801472670/org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest!org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest")
    INFO - Jndi(name="java:global/EjbModule801472670/org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest")
    INFO - Created Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/cdi-interceptors)
    INFO - Undeploying app: /Users/dblevins/examples/cdi-interceptors
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/cdi-interceptors
    INFO - openejb.base = /Users/dblevins/examples/cdi-interceptors
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Beginning load: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-interceptors
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean cdi-interceptors.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean BookShowInterceptorBindingInheritanceExplored: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Enterprise application "/Users/dblevins/examples/cdi-interceptors" loaded.
    INFO - Assembling app: /Users/dblevins/examples/cdi-interceptors
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored!org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied!org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding!org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied!org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/EjbModule1322078637/org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest!org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest")
    INFO - Jndi(name="java:global/EjbModule1322078637/org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest")
    INFO - Created Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookForAShowTwoInterceptorsAppiledTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/cdi-interceptors)
    INFO - Undeploying app: /Users/dblevins/examples/cdi-interceptors
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.479 sec
    Running org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/cdi-interceptors
    INFO - openejb.base = /Users/dblevins/examples/cdi-interceptors
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Beginning load: /Users/dblevins/examples/cdi-interceptors/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/cdi-interceptors
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean cdi-interceptors.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Configuring Service(id=Default Stateful Container, type=Container, provider-id=Default Stateful Container)
    INFO - Auto-creating a container for bean BookShowInterceptorBindingInheritanceExplored: Container(type=STATEFUL, id=Default Stateful Container)
    INFO - Enterprise application "/Users/dblevins/examples/cdi-interceptors" loaded.
    INFO - Assembling app: /Users/dblevins/examples/cdi-interceptors
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/cdi-interceptors.Comp")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored!org.superbiz.cdi.bookshow.beans.BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookShowInterceptorBindingInheritanceExplored")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding!org.superbiz.cdi.bookshow.beans.BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOldStyleInterceptorBinding")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied!org.superbiz.cdi.bookshow.beans.BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowTwoInterceptorsApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied!org.superbiz.cdi.bookshow.beans.BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/cdi-interceptors/BookForAShowOneInterceptorApplied")
    INFO - Jndi(name="java:global/EjbModule1335770660/org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest!org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest")
    INFO - Jndi(name="java:global/EjbModule1335770660/org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest")
    INFO - Created Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Created Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BookForAShowOneInterceptorApplied, ejb-name=BookForAShowOneInterceptorApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowTwoInterceptorsApplied, ejb-name=BookForAShowTwoInterceptorsApplied, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookShowInterceptorBindingInheritanceExplored, ejb-name=BookShowInterceptorBindingInheritanceExplored, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=BookForAShowOldStyleInterceptorBinding, ejb-name=BookForAShowOldStyleInterceptorBinding, container=Default Stateful Container)
    INFO - Started Ejb(deployment-id=cdi-interceptors.Comp, ejb-name=cdi-interceptors.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest, ejb-name=org.superbiz.cdi.bookshow.interceptors.BookShowInterceptorBindingInheritanceTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/cdi-interceptors)
    Intercepted by:[BookForAShowLoggingInterceptor, TimeBasedRestrictingInterceptor]
    INFO - Undeploying app: /Users/dblevins/examples/cdi-interceptors
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.264 sec
    
    Results :
    
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
    
