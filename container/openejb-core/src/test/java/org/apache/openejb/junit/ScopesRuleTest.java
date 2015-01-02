package org.apache.openejb.junit;

import org.apache.openejb.testing.ContainerProperties;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.RequestScoped;

import static org.junit.Assert.assertTrue;

@ContainerProperties({
        @ContainerProperties.Property(name = "openejb.testing.start-cdi-contexts", value = "false")
})
@org.apache.openejb.testing.Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class ScopesRuleTest {
    @Rule
    public final ScopesRule rule = new ScopesRule();

    public static class Foo {
        public void touch() {
            // ok
        }
    }

    @Test
    @CdiScopes(RequestScoped.class)
    public void scopeExists() {
        assertTrue(WebBeansContext.currentInstance().getContextsService().getCurrentContext(RequestScoped.class).isActive());
    }
}
