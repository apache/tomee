package org.apache.tomee.microprofile.jwt;

import com.nimbusds.jose.JWSSigner;
import org.apache.openejb.loader.JarLocation;
import org.eclipse.microprofile.jwt.tck.TCKConstants;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class AppDeploymentExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeploymentScenarioGenerator.class, SimpleDeploymentScenarioGenerator.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, MPJWTTCKArchiveProcess.class);
    }

    public static class SimpleDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

        private final DeploymentScenarioGenerator standard = new AnnotationDeploymentScenarioGenerator();
        private final DeploymentDescription emptyTestWebApp;

        public SimpleDeploymentScenarioGenerator() {
            emptyTestWebApp = new DeploymentDescription("mp-jwt-tck.war",
                    ShrinkWrap
                            .create(WebArchive.class, "mp-jwt-tck.war")
                            .addAsLibrary(JarLocation.jarLocation(TokenUtils.class))
                            .addAsLibrary(JarLocation.jarLocation(JWSSigner.class))
                            .addAsLibrary(JarLocation.jarLocation(TCKConstants.class).getAbsolutePath().replace("-tests.jar", "-test-sources.jar"))
                            .add(EmptyAsset.INSTANCE, "WEB-INF/beans.xml"));
        }


        @Override
        public List<DeploymentDescription> generate(final TestClass testClass) {
            final List<DeploymentDescription> stdDeploymentDescriptions = standard.generate(testClass);

            if (stdDeploymentDescriptions != null && !stdDeploymentDescriptions.isEmpty()) {
                return stdDeploymentDescriptions;
            }

            return Collections.singletonList(emptyTestWebApp);
        }
    }

    /**
     * An ApplicationArchiveProcessor for the MP-JWT TCK if needed
     * With the current implementation we don't need to do anything
     */
    public static class MPJWTTCKArchiveProcess implements ApplicationArchiveProcessor {
        private static Logger log = Logger.getLogger(MPJWTTCKArchiveProcess.class.getName());

        @Override
        public void process(final Archive<?> appArchive, final TestClass testClass) {
            if (!(appArchive instanceof WebArchive)) {
                return;
            }
            final WebArchive war = WebArchive.class.cast(appArchive);
            war.addClass(JWTAuthContextInfoProvider.class);

            log.info("Augmented war: \n"+war.toString(true));
        }
    }
}