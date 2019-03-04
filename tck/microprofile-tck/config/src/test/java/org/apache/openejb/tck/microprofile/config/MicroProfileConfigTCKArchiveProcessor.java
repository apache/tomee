package org.apache.openejb.tck.microprofile.config;

import org.hamcrest.object.HasToString;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class MicroProfileConfigTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            final WebArchive war = WebArchive.class.cast(archive);
            war.addAsLibrary(jarLocation(Assert.class));
            war.addAsLibrary(jarLocation(HasToString.class));
        }
    }
}
