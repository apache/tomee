import org.apache.openejb.BeanContext;
import org.apache.openejb.bval.BeanValidationAppendixInterceptor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.designbycontract.OlympicGamesManager;
import org.superbiz.designbycontract.PoleVaultingManager;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.validation.ConstraintViolationException;

import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author Romain Manni-Bucau
 */
public class OlympicGamesTest {
    private static Context context;

    @EJB private OlympicGamesManager gamesManager;
    @EJB private PoleVaultingManager poleVaultingManager;

    @BeforeClass public static void start() {
        Properties properties = new Properties();
        properties.setProperty(BeanContext.USER_INTERCEPTOR_KEY, BeanValidationAppendixInterceptor.class.getName());
        context = EJBContainer.createEJBContainer(properties).getContext();
    }

    @Before public void inject() throws Exception {
        context.bind("inject", this);
    }

    @AfterClass public static void stop() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    @Test public void sportMenOk() throws Exception {
        assertEquals("IWin [FR]", gamesManager.addSportMan("IWin", "FR"));
    }

    @Test public void sportMenKoBecauseOfName() throws Exception {
        try {
            gamesManager.addSportMan("I lose", "EN");
            fail("no space should be in names");
        } catch (EJBException wrappingException) {
            assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
            assertEquals(1, exception.getConstraintViolations().size());
        }
    }

    @Test public void sportMenKoBecauseOfCountry() throws Exception {
        try {
            gamesManager.addSportMan("ILoseTwo", "TOO-LONG");
            fail("country should be between 2 and 4 characters");
        } catch (EJBException wrappingException) {
            assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
            assertEquals(1, exception.getConstraintViolations().size());
        }
    }

    @Test public void polVaulting() throws Exception {
        assertEquals(100, poleVaultingManager.points(220));
    }

    @Test public void tooShortPolVaulting() throws Exception {
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
