package org.apache.tomee.microprofile.tck.config;

import org.eclipse.microprofile.config.tck.converters.UpperCaseDuckConverter;
import org.hamcrest.object.HasToString;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class MicroProfileConfigTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            final WebArchive war = WebArchive.class.cast(archive);

            // TODO - this could be fixed in the TCK by adding UpperCaseDuckConverter into org.eclipse.microprofile.config.tck.ConverterTest
            final JavaArchive configJar = ShrinkWrap.create(JavaArchive.class, "config-tck-additional.jar")
                                                    .addClass(UpperCaseDuckConverter.class);

            war.addAsLibraries(configJar);
            war.addAsLibrary(jarLocation(HasToString.class));
        }
    }
}
