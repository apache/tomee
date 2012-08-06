package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(ApplicationComposer.class)
public class WebbeansContextInEmbeddedModeTest {
    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @Test
    public void checkWebbeansContext() {
        final WebBeansContext ctx1 = WebBeansContext.currentInstance();
        final List<AppContext> appCtxs = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();
        assertEquals(1, appCtxs.size());
        final WebBeansContext ctx2 = appCtxs.iterator().next().getWebBeansContext();
        assertSame(ctx1, ctx2);
    }
}
