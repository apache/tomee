package org.apache.tomee.microprofile.jwt;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.Collections;
import java.util.List;

public class AppDeploymentExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeploymentScenarioGenerator.class, SimpleDeploymentScenarioGenerator.class);
    }

    public static class SimpleDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

        private final DeploymentScenarioGenerator standard = new AnnotationDeploymentScenarioGenerator();

        @Override
        public List<DeploymentDescription> generate(final TestClass testClass) {
            final List<DeploymentDescription> stdDeploymentDescriptions = standard.generate(testClass);

            if (stdDeploymentDescriptions != null && !stdDeploymentDescriptions.isEmpty()) {
                return stdDeploymentDescriptions;
            }

            return Collections.singletonList(new DeploymentDescription("test.war",
                    ShrinkWrap.create(WebArchive.class, "test.war").add(EmptyAsset.INSTANCE, "WEB-INF/beans.xml")));
        }
    }
}