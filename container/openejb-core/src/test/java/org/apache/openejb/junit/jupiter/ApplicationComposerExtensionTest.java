package org.apache.openejb.junit.jupiter;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(ApplicationComposerExtension.class)
public class ApplicationComposerExtensionTest {

    @Module
    @org.apache.openejb.testing.Classes(cdi = true, value = SomeCDIClass.class)
    public EjbJar classes() {
        return new EjbJar();
    }

    @Inject
    private SomeCDIClass someCDIClass;

    @Test
    public void checkInjection() {
        assertNotNull(someCDIClass);
    }

    @ApplicationScoped
    public static class SomeCDIClass {

    }

}
