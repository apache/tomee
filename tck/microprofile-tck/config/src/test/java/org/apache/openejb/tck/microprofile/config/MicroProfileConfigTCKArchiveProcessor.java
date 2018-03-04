package org.apache.openejb.tck.microprofile.config;

import org.eclipse.microprofile.config.tck.converters.UpperCaseDuckConverter;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

public class MicroProfileConfigTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            // TODO - this could be fixed in the TCK by adding UpperCaseDuckConverter into org.eclipse.microprofile.config.tck.ConverterTest
            JavaArchive configJar = ShrinkWrap
                    .create(JavaArchive.class, "config-tck-additional.jar")
                    .addClass(UpperCaseDuckConverter.class)
                    ;
            ((WebArchive) archive).addAsLibraries(configJar);

            File[] requiredLibraries = Maven.resolver()
                                            .loadPomFromFile("pom.xml")
                                            .resolve("org.hamcrest:hamcrest-all:1.3")
                                            .withTransitivity()
                                            .asFile();
            ((WebArchive) archive).addAsLibraries(requiredLibraries);
        }
    }
}
