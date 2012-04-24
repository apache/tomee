package org.apache.openejb.junit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
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
public class TestEJBContainerRunnerResources {
    @org.apache.openejb.junit.jee.resources.TestResource
    private Context ctx;

    @org.apache.openejb.junit.jee.resources.TestResource
    private java.util.Properties props;

    @org.apache.openejb.junit.jee.resources.TestResource
    private EJBContainer container;

    @Test
    public void checkCtx() {
        assertNotNull(ctx);
    }

    @Test
    public void checkProps() {
        assertNotNull(props);
    }

    @Test
    public void checkContainer() {
        assertNotNull(container);
    }
}
