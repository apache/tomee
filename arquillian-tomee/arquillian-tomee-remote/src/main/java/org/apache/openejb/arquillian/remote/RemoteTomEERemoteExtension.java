package org.apache.openejb.arquillian.remote;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

public class RemoteTomEERemoteExtension implements RemoteLoadableExtension {
    @Override public void register(ExtensionBuilder builder) {
       builder.observer(RemoteTomEEObserver.class);
    }
}
