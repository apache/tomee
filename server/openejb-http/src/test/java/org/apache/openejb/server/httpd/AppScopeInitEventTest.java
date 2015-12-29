package org.apache.openejb.server.httpd;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Component;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SimpleLog
@EnableServices("http")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class AppScopeInitEventTest {
    @Component
    public ServletContext context() { // default one doesnt read WebApp
        final EmbeddedServletContext servletContext = new EmbeddedServletContext();
        servletContext.setInitParameter("test", "start");
        return servletContext;
    }

    @Inject
    private Start start;

    @Inject
    private ServletContext context;

    @Test
    public void checkAccessAtStartup() {
        assertNotNull(start.getContext());
        assertEquals("start", start.getValue());
    }

    @Test
    public void checkAccessAtRuntime() {
        assertEquals("start", start.getContext().getInitParameter("test"));
        assertEquals("start", context.getInitParameter("test"));
    }

    @ApplicationScoped
    public static class Start {
        private volatile ServletContext context;
        private volatile String value;

        private void capture(@Observes @Initialized(ApplicationScoped.class) final ServletContext context) {
            if (this.context != null) {
                throw new IllegalStateException("app context started twice");
            }

            this.context = context;
            this.value = context.getInitParameter("test");
        }

        public ServletContext getContext() {
            return context;
        }

        public String getValue() {
            return value;
        }
    }
}
