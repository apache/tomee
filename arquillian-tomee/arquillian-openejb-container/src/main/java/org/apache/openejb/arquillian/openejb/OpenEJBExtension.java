package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class OpenEJBExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeployableContainer.class, OpenEJBDeployableContainer.class)
            .service(TestEnricher.class, OpenEJBInjectionEnricher.class)
            .service(ApplicationArchiveProcessor.class, OpenEJBArchiveProcessor.class);
    }
}
