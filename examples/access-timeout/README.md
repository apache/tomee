Title: @AccessTimeout annotation

In a general sense this annotation portably specifies up to how long a caller will wait if a wait condition occurs with concurrent access.  Specific to each bean type, wait conditions will occur when:

 - `@Singleton` - an `@Lock(WRITE)` method is being invoked and container-managed concurrency is being used.  All methods are `@Lock(WRITE)` by default.
 - `@Stateful` - any method of the instance is being invoked and a second invocation occurs.  OR the @Stateful bean is in a transaction and the caller is invoking it from outside that transaction.
 - `@Stateless` - no instances are available in the pool. As noted, however, pooling sematics, if any, are not covered by the spec.  If the vendor's pooling semantics do involve a wait condition, the @AccessTimeout should apply.

The `@AccessTimeout` is simply a convenience wrapper around the `long` and `TimeUnit` tuples commonly used in the `java.util.concurrent` API.

    import java.util.concurrent.TimeUnit;
    @Target({METHOD, TYPE})
    @Retention(RUNTIME)
    public @interface AccessTimeout {
        long value();
        TimeUnit unit() default TimeUnit.MILLISECONDS;
    }

When explicitly set on a bean class or method, it has three possible meanings:

 - `@AccessTimeout(-1)` - Never timeout, wait as long as it takes.  Potentially forever.
 - `@AccessTimeout(0)` - Never wait. Immediately throw `ConcurrentAccessException` if a wait condition would have occurred.
 - `@AccessTimout(30, TimeUnit.SECONDS)` - Wait up to 30 seconds if a wait condition occurs.  After that, throw `ConcurrentAccessTimeoutExcpetion`

## No standard default

Note that the `value` attribute has no default.  This was intentional and intended to communicate that if `@AccessTimeout` is not explicitly used, the behavior you get is vendor-specific.

Some vendors will wait for a preconfigured time and throw `javax.ejb.ConcurrentAccessException`, some vendors will throw it immediately.  When we were defining this annotation it became clear that all of us vendors were doing things a bit differently and enforcing a default would cause problems for existing apps.

On a similar note, prior to EJB 3.0 there was no default transaction attribute and it was different for every vendor.  Thank goodness EJB 3.0 was different enough that we could finally say, "For EJB 3.0 beans the default is REQUIRED."

# Example

Here we have a simple @Singleton bean that has three synchronous methods and one `@Asynchronous` method.  The bean itself is annotated `@Lock(WRITE)` so that only one thread may access the `@Singleton` at a time.  This is the default behavior of an `@Singleton` bean, so explicit usage of `@Lock(WRITE)` is not needed but is rather nice for clarity as the single-threaded nature of the bean is important to the example.

    @Singleton
    @Lock(WRITE)
    public class BusyBee {

        @Asynchronous
        public Future stayBusy(CountDownLatch ready) {
            ready.countDown();

            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            return null;
        }

        @AccessTimeout(0)
        public void doItNow() {
            // do something
        }

        @AccessTimeout(value = 5, unit = TimeUnit.SECONDS)
        public void doItSoon() {
            // do something
        }

        @AccessTimeout(-1)
        public void justDoIt() {
            // do something
        }

    }

The `@Asynchronous` method is not a critical part of `@AccessTimeout`, but serves as a simple way to "lock" the bean for testing purposes.  It allows us to easily test the concurrent behavior of the bean.

    public class BusyBeeTest extends TestCase {

        public void test() throws Exception {

            final Context context = EJBContainer.createEJBContainer().getContext();

            final CountDownLatch ready = new CountDownLatch(1);

            final BusyBee busyBee = (BusyBee) context.lookup("java:global/access-timeout/BusyBee");

            // This asynchronous method will never exit
            busyBee.stayBusy(ready);

            // Are you working yet little bee?
            ready.await();


            // OK, Bee is busy


            { // Timeout Immediately
                final long start = System.nanoTime();

                try {
                    busyBee.doItNow();

                    fail("The bee should be busy");
                } catch (Exception e) {
                    // the bee is still too busy as expected
                }

                assertEquals(0, seconds(start));
            }

            { // Timeout in 5 seconds
                final long start = System.nanoTime();

                try {
                    busyBee.doItSoon();

                    fail("The bee should be busy");
                } catch (Exception e) {
                    // the bee is still too busy as expected
                }

                assertEquals(5, seconds(start));
            }

            // This will wait forever, give it a try if you have that long
            //busyBee.justDoIt();
        }

        private long seconds(long start) {
            return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
        }
    }


## Running


    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.accesstimeout.BusyBeeTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/access-timeout
    INFO - openejb.base = /Users/dblevins/examples/access-timeout
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/access-timeout/target/classes
    INFO - Beginning load: /Users/dblevins/examples/access-timeout/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/access-timeout
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean BusyBee: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.accesstimeout.BusyBeeTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/access-timeout" loaded.
    INFO - Assembling app: /Users/dblevins/examples/access-timeout
    INFO - Jndi(name="java:global/access-timeout/BusyBee!org.superbiz.accesstimeout.BusyBee")
    INFO - Jndi(name="java:global/access-timeout/BusyBee")
    INFO - Jndi(name="java:global/EjbModule748454644/org.superbiz.accesstimeout.BusyBeeTest!org.superbiz.accesstimeout.BusyBeeTest")
    INFO - Jndi(name="java:global/EjbModule748454644/org.superbiz.accesstimeout.BusyBeeTest")
    INFO - Created Ejb(deployment-id=org.superbiz.accesstimeout.BusyBeeTest, ejb-name=org.superbiz.accesstimeout.BusyBeeTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=BusyBee, ejb-name=BusyBee, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.accesstimeout.BusyBeeTest, ejb-name=org.superbiz.accesstimeout.BusyBeeTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=BusyBee, ejb-name=BusyBee, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/access-timeout)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.071 sec

    Results :

    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

