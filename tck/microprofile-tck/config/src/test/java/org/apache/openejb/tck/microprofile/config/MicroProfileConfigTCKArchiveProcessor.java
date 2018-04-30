package org.apache.openejb.tck.microprofile.config;

import static org.apache.openejb.loader.JarLocation.jarLocation;

import org.eclipse.microprofile.config.tck.converters.UpperCaseDuckConverter;
import org.hamcrest.object.HasToString;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;

public class MicroProfileConfigTCKArchiveProcessor implements ApplicationArchiveProcessor {
    private File hamcrest;

    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            final WebArchive war = WebArchive.class.cast(archive);

            // TODO - this could be fixed in the TCK by adding UpperCaseDuckConverter into org.eclipse.microprofile.config.tck.ConverterTest
            JavaArchive configJar = ShrinkWrap
                    .create(JavaArchive.class, "config-tck-additional.jar")
                    .addClass(UpperCaseDuckConverter.class)
                    ;

            war.addAsLibraries(configJar);

            if (hamcrest == null) {
                hamcrest = jarLocation(HasToString.class);
            }
            war.addAsLibrary(hamcrest);
        }
    }
}
