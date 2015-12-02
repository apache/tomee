package org.apache.openejb.junit;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ContainerAndApplicationRulesTest {
    private final ContainerRule instanceContainer = new ContainerRule(new Container());
    private final ApplicationRule instanceServer = new ApplicationRule(new App());

    @Rule
    public final TestRule rule = RuleChain.outerRule(instanceContainer).around(instanceServer);

    @Test
    public void test() {
        assertNotNull(instanceServer.getInstance(App.class).v);
        assertNull(instanceContainer.getInstance(Container.class).ignored);
    }

    @org.apache.openejb.testing.Classes(cdi = true, value = Ignored.class) // @Classes invalid for a container
    public static class Container {
        @Inject
        private Provider<Ignored> ignored;
    }

    @PersistenceUnitDefinition
    @org.apache.openejb.testing.Classes(context = "App1", cdi = true, value = Valuable.class)
    public static class App {
        @Inject
        private Valuable v;
    }

    public static class Ignored {
    }

    public static class Valuable {
    }
}
