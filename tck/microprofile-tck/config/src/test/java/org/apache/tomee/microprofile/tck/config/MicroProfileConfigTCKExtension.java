package org.apache.tomee.microprofile.tck.config;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class MicroProfileConfigTCKExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, MicroProfileConfigTCKArchiveProcessor.class);
    }
}
