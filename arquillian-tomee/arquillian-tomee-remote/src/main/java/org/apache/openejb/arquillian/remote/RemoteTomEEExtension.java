package org.apache.openejb.arquillian.remote;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class RemoteTomEEExtension implements LoadableExtension {
    @Override public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, RemoteTomEEContainer.class)
            .service(AuxiliaryArchiveAppender.class, RemoteTomEEEJBEnricherArchiveAppender.class);
    }
}
