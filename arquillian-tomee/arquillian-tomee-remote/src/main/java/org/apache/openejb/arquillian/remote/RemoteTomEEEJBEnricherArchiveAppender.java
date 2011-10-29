package org.apache.openejb.arquillian.remote;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class RemoteTomEEEJBEnricherArchiveAppender implements AuxiliaryArchiveAppender {
    @Override public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-tomee-testenricher-ejb.jar")
                   .addClasses(RemoteTomEEObserver.class, RemoteTomEERemoteExtension.class)
                   .addAsServiceProvider(RemoteLoadableExtension.class, RemoteTomEERemoteExtension.class);
    }
}