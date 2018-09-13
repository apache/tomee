package org.apache.tomee.microprofile.jwt;

import com.nimbusds.jose.JWSSigner;
import org.apache.openejb.loader.JarLocation;
import org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration;
import org.apache.tomee.arquillian.remote.RemoteTomEEContainer;
import org.eclipse.microprofile.jwt.tck.TCKConstants;
import org.eclipse.microprofile.jwt.tck.config.IssValidationTest;
import org.eclipse.microprofile.jwt.tck.config.PublicKeyAsPEMLocationTest;
import org.eclipse.microprofile.jwt.tck.config.PublicKeyAsPEMTest;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.NodeImpl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

        @Inject
        private Instance<ContainerRegistry> containerRegistry;

        @Override
        public void process(final Archive<?> appArchive, final TestClass testClass) {
            if (!(appArchive instanceof WebArchive)) {
                return;
            }
            final WebArchive war = WebArchive.class.cast(appArchive);
            war.addClass(JWTAuthContextInfoProvider.class);

            // MP Config in wrong place - See https://github.com/eclipse/microprofile/issues/46.
            final Map<ArchivePath, Node> content = war.getContent(object -> object.get().matches(".*META-INF/.*"));
            content.forEach((archivePath, node) -> war.addAsResource(node.getAsset(), node.getPath()));

            // Spec says that vendor specific ways to load the keys take precedence, so we need to remove it in test
            // cases that use the Config approach.
            Stream.of(
                    PublicKeyAsPEMTest.class,
                    PublicKeyAsPEMLocationTest.class,
                    IssValidationTest.class)
                  .filter(c -> c.equals(testClass.getJavaClass()))
                  .findAny()
                  .ifPresent(c -> war.deleteClass(JWTAuthContextInfoProvider.class));

            // Rewrite the correct server port in configuration
            final Container container = containerRegistry.get().getContainer(TargetDescription.DEFAULT);
            if (container.getDeployableContainer() instanceof RemoteTomEEContainer) {
                final RemoteTomEEContainer remoteTomEEContainer =
                        (RemoteTomEEContainer) container.getDeployableContainer();
                final RemoteTomEEConfiguration configuration = remoteTomEEContainer.getConfiguration();
                final String httpPort = configuration.getHttpPort() + "";

                final Map<ArchivePath, Node> microprofileProperties =
                        war.getContent(object -> object.get().matches(".*META-INF/microprofile-config\\.properties"));
                microprofileProperties.forEach((archivePath, node) -> {
                    try {
                        final Properties properties = new Properties();
                        properties.load(node.getAsset().openStream());
                        properties.replaceAll((key, value) -> ((String) value).replaceAll("8080", httpPort));
                        final StringWriter stringWriter = new StringWriter();
                        properties.store(stringWriter, null);
                        war.delete(archivePath);
                        war.add(new StringAsset(stringWriter.toString()), node.getPath());
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            log.info("Augmented war: \n"+war.toString(true));
        }
    }
}
