index-group=Transactions
type=page
status=published
title=@ApplicationException annotation
~~~~~~

# Declaring an @ApplicationException

    import javax.ejb.ApplicationException;

    /**
     * @version $Rev: 784112 $ $Date: 2009-06-12 06:23:57 -0700 (Fri, 12 Jun 2009) $
     */
    @ApplicationException(rollback = true)
    public abstract class BusinessException extends RuntimeException {
    }

By default, @ApplicationException is inherited

    public class ValueRequiredException extends BusinessException {
    }

# In the bean code

    @Stateless
    public class ThrowBusinessExceptionImpl implements ThrowBusinessException {

        public void throwValueRequiredException() throws BusinessException {
            throw new ValueRequiredException();
        }

    }

Normally throwing a `RuntimeException` would cause the container to both rollback the transaction and destroy the bean instance that threw the exception.

As `BusinessException` has been annotated `@ApplicationException(rollback = true)` only the transaction rollback will occur and the bean will not get destroyed.

# The TestCase

    import org.junit.Assert;
    import org.junit.Before;
    import org.junit.Test;

    import javax.naming.Context;
    import javax.naming.InitialContext;
    import java.util.Properties;

    public class ThrowBusinessExceptionImplTest {

        private InitialContext initialContext;

        @Before
        public void setUp() throws Exception {
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

            initialContext = new InitialContext(properties);
        }

        @Test(expected = ValueRequiredException.class)
        public void testCounterViaRemoteInterface() throws Exception {
            Object object = initialContext.lookup("ThrowBusinessExceptionImplRemote");

            Assert.assertNotNull(object);
            Assert.assertTrue(object instanceof ThrowBusinessException);
            ThrowBusinessException bean = (ThrowBusinessException) object;
            bean.throwValueRequiredException();
        }
    }

# Running

    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.appexception.ThrowBusinessExceptionImplTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/applicationexception
    INFO - openejb.base = /Users/dblevins/examples/applicationexception
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/applicationexception/target/classes
    INFO - Beginning load: /Users/dblevins/examples/applicationexception/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/applicationexception/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean ThrowBusinessExceptionImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/applicationexception/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/applicationexception/classpath.ear
    INFO - Jndi(name=ThrowBusinessExceptionImplRemote) --> Ejb(deployment-id=ThrowBusinessExceptionImpl)
    INFO - Jndi(name=global/classpath.ear/applicationexception/ThrowBusinessExceptionImpl!org.superbiz.appexception.ThrowBusinessException) --> Ejb(deployment-id=ThrowBusinessExceptionImpl)
    INFO - Jndi(name=global/classpath.ear/applicationexception/ThrowBusinessExceptionImpl) --> Ejb(deployment-id=ThrowBusinessExceptionImpl)
    INFO - Created Ejb(deployment-id=ThrowBusinessExceptionImpl, ejb-name=ThrowBusinessExceptionImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=ThrowBusinessExceptionImpl, ejb-name=ThrowBusinessExceptionImpl, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/applicationexception/classpath.ear)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.434 sec

    Results :

    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
