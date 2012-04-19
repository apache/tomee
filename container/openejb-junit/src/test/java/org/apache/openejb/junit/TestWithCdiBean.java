package org.apache.openejb.junit;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.junit.ejbcontainer.EJBContainerRunner;
import org.apache.openejb.junit.ejbcontainer.Properties;
import org.apache.openejb.junit.ejbcontainer.Property;
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

    @Test
    public void checkInjections() {
        assertNotNull(cdi);
        assertNotNull(ejb);
    }

    public static class CdiBean {}

    @Stateless
    public static class EjbBean {}
}
