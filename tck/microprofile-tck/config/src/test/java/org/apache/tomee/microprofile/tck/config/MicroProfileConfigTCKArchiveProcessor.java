package org.apache.tomee.microprofile.tck.config;

import org.hamcrest.object.HasToString;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class MicroProfileConfigTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            WebArchive.class.cast(archive).addAsLibrary(jarLocation(HasToString.class));
        }
    }
}
