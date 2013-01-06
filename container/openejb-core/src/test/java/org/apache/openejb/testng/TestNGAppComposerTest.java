package org.apache.openejb.testng;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.testing.Module;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ejb.EJB;
import javax.transaction.SystemException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Listeners(ApplicationComposerListener.class)
public class TestNGAppComposerTest {
    @EJB
    private TestNGSingleton singleton;

    @Module
    public EnterpriseBean singleton() {
        return new SingletonBean(TestNGSingleton.class).localBean();
    }

    @Test
    public void notNull() {
        assertNotNull(singleton);
    }

    @Test
    public void ejb() {
        assertTrue(singleton.ejb());
    }

    public static class TestNGSingleton {
        public boolean ejb() {
            try {
                return OpenEJB.getTransactionManager().getTransaction() != null;
            } catch (SystemException e) {
                return false;
            }
        }
    }
}
