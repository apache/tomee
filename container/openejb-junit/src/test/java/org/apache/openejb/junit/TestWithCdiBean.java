package org.apache.openejb.junit;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.junit.jee.EJBContainerRunner;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@Properties({ // just a small conf to go faster
    @Property(key = DeploymentFilterable.CLASSPATH_EXCLUDE, value = "jar:.*")
})
@RunWith(EJBContainerRunner.class)
public class TestWithCdiBean {
    @Inject
    private CdiBean cdi;

    @Inject
    private EjbBean ejb;

    @EJB
    private EjbBean ejb2;

    @Test
    public void checkCDIInjections() {
        assertNotNull(cdi);
        assertNotNull(ejb);
    }

    @Test
    public void checkEJBInjection() {
        assertNotNull(ejb2);
    }

    public static class CdiBean {}

    @Stateless
    public static class EjbBean {}
}
