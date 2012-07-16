package org.superbiz.event;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ListenerTest {
    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class, "listener-test.jar")
                .addClasses(MyListener.class, AutoDiscoveredListener.class)
                .addAsManifestResource(new StringAsset(AutoDiscoveredListener.class.getName()), ArchivePaths.create("org.apache.openejb.extension"));
    }

    @Test
    public void check() {
        assertEquals("listener-test", AutoDiscoveredListener.getAppName());
    }
}
