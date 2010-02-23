package org.apache.openejb.junit;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.annotations.RunTestAs;
import org.apache.openejb.junit.ejbs.BasicEjbLocal;
import org.apache.openejb.junit.ejbs.SecuredEjbLocal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;

/**
 * @author quintin
 */
@RunWith(OpenEjbRunner.class)
@RunTestAs("RoleA")
@LocalClient
public class TestEjbSecurityRunTestAs {
    @EJB
    private BasicEjbLocal basicEjb;

    @EJB
    private SecuredEjbLocal securedEjb;

    public TestEjbSecurityRunTestAs() {
    }

    @Test
    public void testEjbInjection() {
        assertNotNull(basicEjb);
        assertNotNull(securedEjb);
    }

    @Test
    public void testClassLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleA Works", securedEjb.roleA());

        try {
            securedEjb.roleB();
            fail("Able to execute a method for which we shouldn't have access.");
        }
        catch (EJBAccessException e) {
        }
    }

    @Test
    @RunTestAs("RoleB")
    public void testMethodLevelSecurity() {
        assertNotNull(securedEjb);

        assertEquals("Unsecured Works", basicEjb.concat("Unsecured", "Works"));
        assertEquals("Dual Role Works", securedEjb.dualRole());
        assertEquals("RoleB Works", securedEjb.roleB());

        try {
            securedEjb.roleA();
            fail("Able to execute a method for which we shouldn't have access.");
        }
        catch (EJBAccessException e) {
        }
    }
}
