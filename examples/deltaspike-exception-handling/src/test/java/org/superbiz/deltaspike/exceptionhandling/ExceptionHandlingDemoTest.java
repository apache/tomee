package org.superbiz.deltaspike.exceptionhandling;

import javax.inject.Inject;
import org.apache.deltaspike.core.impl.config.DefaultConfigSourceProvider;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ExceptionHandlingDemoTest {
    @Inject
    private OSValidator validator;

    @Deployment
    public static WebArchive jar() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(OSValidator.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                // DeltaSpike libs (api and impl)
                .addAsLibraries(JarLocation.jarLocation(ConfigSourceProvider.class))
                .addAsLibraries(JarLocation.jarLocation(DefaultConfigSourceProvider.class));
    }

    @Test
    public void check() {
        assertNotNull(validator);
        validator.validOS("Linux");
        validator.validOS("windows"); // should throw a handled exception
        validator.validOS("Mac OS");
    }
}
