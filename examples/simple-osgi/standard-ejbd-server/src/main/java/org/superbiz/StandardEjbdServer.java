package org.superbiz;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.localclient.LocalInitialContextFactory;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public final class StandardEjbdServer {
    private StandardEjbdServer() {
        // no-op
    }

    public static void main(String[] args) throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, Boolean.TRUE.toString());
        final EJBContainer container = EJBContainer.createEJBContainer(properties);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                latch.countDown();
                container.close();
            }
        });

        latch.await();
    }
}
