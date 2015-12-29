package org.apache.openejb.cdi;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@SimpleLog
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class AppScopeInitEventTest { // servlet context not present without config cause we miss http module
    @Inject
    private Start start;

    @Test
    public void checkAccessAtStartup() {
        assertNotNull(start.getContext());
    }

    @ApplicationScoped
    public static class Start {
        private volatile Object context;

        // ensure we start only once
        private void capture(@Observes @Initialized(ApplicationScoped.class) final Object context) {
            if (this.context != null) {
                throw new IllegalStateException("app context started twice");
            }

            this.context = context;
        }

        public Object getContext() {
            return context;
        }
    }
}
