package org.apache.openejb.config;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HooksTest {
    @BeforeClass
    public static void init() {
        SystemInstance.get().getProperties().putAll(new Properties() {{
            setProperty("run", "new://InitHooks?name=" + HookRun.class.getName());
            setProperty("lifecycle", "new://InitHooks?name=" + HookLifecycle.class.getName());
        }});
    }

    @AfterClass
    public static void reset() {
        SystemInstance.reset();
    }

    @Test
    public void check() throws OpenEJBException {
        final ConfigurationFactory cf = new ConfigurationFactory();
        cf.getOpenEjbConfiguration();
        assertTrue(HookRun.ok);
        assertTrue(HookLifecycle.start);
        assertFalse(HookLifecycle.stop);
        cf.destroy();
        assertTrue(HookLifecycle.stop);
    }

    public static class HookRun implements Runnable {
        private static boolean ok = false;

        @Override
        public void run() {
            ok = true;
        }
    }

    public static class HookLifecycle {
        private static boolean start = false;
        private static boolean stop = false;

        @PostConstruct
        public void start() {
            start = true;
        }

        @PreDestroy
        public void stop() {
            stop = true;
        }
    }
}
