index-group=Unrevised
type=page
status=published
~~~~~~
# Bean Validation - Design By Contract

Bean Validation (aka JSR 303) contains an optional appendix dealing with method validation.

Some implementions of this JSR implement this appendix (Apache bval, Hibernate validator for example).

OpenEJB provides an interceptor which allows you to use this feature to do design by contract.

# Design by contract

The goal is to be able to configure with a finer grain your contract. In the example you specify
the minimum centimeters a sport man should jump at pole vaulting:

    @Local
    public interface PoleVaultingManager {
        int points(@Min(120) int centimeters);
    }

# Usage

TomEE and OpenEJB do not provide anymore `BeanValidationAppendixInterceptor` since
Bean Validation 1.1 does it (with a slighly different usage but the exact same feature).

So basically you don't need to configure anything to use it.
# Errors

If a parameter is not validated an exception is thrown, it is an EJBException wrapping a ConstraintViolationException:

    try {
        gamesManager.addSportMan("I lose", "EN");
        fail("no space should be in names");
    } catch (EJBException wrappingException) {
        assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
        ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
        assertEquals(1, exception.getConstraintViolations().size());
    }

# Example

## OlympicGamesManager

    package org.superbiz.designbycontract;

    import javax.ejb.Stateless;
    import javax.validation.constraints.NotNull;
    import javax.validation.constraints.Pattern;
    import javax.validation.constraints.Size;

    @Stateless
    public class OlympicGamesManager {
        public String addSportMan(@Pattern(regexp = "^[A-Za-z]+$") String name, @Size(min = 2, max = 4) String country) {
            if (country.equals("USA")) {
                return null;
            }
            return new StringBuilder(name).append(" [").append(country).append("]").toString();
        }
    }

## PoleVaultingManager

    package org.superbiz.designbycontract;

    import javax.ejb.Local;
    import javax.validation.constraints.Min;

    @Local
    public interface PoleVaultingManager {
        int points(@Min(120) int centimeters);
    }

## PoleVaultingManagerBean

    package org.superbiz.designbycontract;

    import javax.ejb.Stateless;

    @Stateless
    public class PoleVaultingManagerBean implements PoleVaultingManager {
        @Override
        public int points(int centimeters) {
            return centimeters - 120;
        }
    }

## OlympicGamesTest

    public class OlympicGamesTest {
        private static Context context;

        @EJB
        private OlympicGamesManager gamesManager;

        @EJB
        private PoleVaultingManager poleVaultingManager;

        @BeforeClass
        public static void start() {
            Properties properties = new Properties();
            properties.setProperty(BeanContext.USER_INTERCEPTOR_KEY, BeanValidationAppendixInterceptor.class.getName());
            context = EJBContainer.createEJBContainer(properties).getContext();
        }

        @Before
        public void inject() throws Exception {
            context.bind("inject", this);
        }

        @AfterClass
        public static void stop() throws Exception {
            if (context != null) {
                context.close();
            }
        }

        @Test
        public void sportMenOk() throws Exception {
            assertEquals("IWin [FR]", gamesManager.addSportMan("IWin", "FR"));
        }

        @Test
        public void sportMenKoBecauseOfName() throws Exception {
            try {
                gamesManager.addSportMan("I lose", "EN");
                fail("no space should be in names");
            } catch (EJBException wrappingException) {
                assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
                ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
                assertEquals(1, exception.getConstraintViolations().size());
            }
        }

        @Test
        public void sportMenKoBecauseOfCountry() throws Exception {
            try {
                gamesManager.addSportMan("ILoseTwo", "TOO-LONG");
                fail("country should be between 2 and 4 characters");
            } catch (EJBException wrappingException) {
                assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
                ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
                assertEquals(1, exception.getConstraintViolations().size());
            }
        }

        @Test
        public void polVaulting() throws Exception {
            assertEquals(100, poleVaultingManager.points(220));
        }

        @Test
        public void tooShortPolVaulting() throws Exception {
            try {
                poleVaultingManager.points(119);
                fail("the jump is too short");
            } catch (EJBException wrappingException) {
                assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
                ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
                assertEquals(1, exception.getConstraintViolations().size());
            }
        }
    }

# Running


    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running OlympicGamesTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/bean-validation-design-by-contract
    INFO - openejb.base = /Users/dblevins/examples/bean-validation-design-by-contract
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/bean-validation-design-by-contract/target/classes
    INFO - Beginning load: /Users/dblevins/examples/bean-validation-design-by-contract/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/bean-validation-design-by-contract
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean PoleVaultingManagerBean: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean OlympicGamesTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/bean-validation-design-by-contract" loaded.
    INFO - Assembling app: /Users/dblevins/examples/bean-validation-design-by-contract
    INFO - Jndi(name="java:global/bean-validation-design-by-contract/PoleVaultingManagerBean!org.superbiz.designbycontract.PoleVaultingManager")
    INFO - Jndi(name="java:global/bean-validation-design-by-contract/PoleVaultingManagerBean")
    INFO - Jndi(name="java:global/bean-validation-design-by-contract/OlympicGamesManager!org.superbiz.designbycontract.OlympicGamesManager")
    INFO - Jndi(name="java:global/bean-validation-design-by-contract/OlympicGamesManager")
    INFO - Jndi(name="java:global/EjbModule236054577/OlympicGamesTest!OlympicGamesTest")
    INFO - Jndi(name="java:global/EjbModule236054577/OlympicGamesTest")
    INFO - Created Ejb(deployment-id=OlympicGamesManager, ejb-name=OlympicGamesManager, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=PoleVaultingManagerBean, ejb-name=PoleVaultingManagerBean, container=Default Stateless Container)
    INFO - Created Ejb(deployment-id=OlympicGamesTest, ejb-name=OlympicGamesTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=OlympicGamesManager, ejb-name=OlympicGamesManager, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=PoleVaultingManagerBean, ejb-name=PoleVaultingManagerBean, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=OlympicGamesTest, ejb-name=OlympicGamesTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/bean-validation-design-by-contract)
    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.245 sec

    Results :

    Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
