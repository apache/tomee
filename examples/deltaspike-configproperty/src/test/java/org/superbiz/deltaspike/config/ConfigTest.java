package org.superbiz.deltaspike.config;

import javax.inject.Inject;
import org.apache.deltaspike.core.impl.config.DefaultConfigSourceProvider;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.openjpa.lib.conf.MapConfigurationProvider;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ConfigTest {
    @Inject
    private Counter counter;

    @Deployment
    public static WebArchive jar() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Counter.class, MyConfigSource.class, MapConfigurationProvider.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsResource(new ClassLoaderAsset("my-app-config.properties"), "my-app-config.properties")
                .addAsLibraries(JarLocation.jarLocation(ConfigSourceProvider.class))
                .addAsLibraries(JarLocation.jarLocation(DefaultConfigSourceProvider.class))
                .addAsServiceProvider(ConfigSourceProvider.class, MyConfigSourceProvider.class);
    }

    @Test
    public void check() {
        assertNotNull(counter);
        counter.loop();
    }
}
