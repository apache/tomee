package org.apache.openejb.arquillian.openejb;

import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.LogCategory;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class OpenEJBExtension implements LoadableExtension {
    private static final String OPENEJB_ADAPTER_NAME = "openejb";

    static { // logging conf
        if (ArquillianUtil.isCurrentAdapter(OPENEJB_ADAPTER_NAME)) {
            if (System.getProperty("java.util.logging.config.class") == null || System.getProperty("java.util.logging.config.file") == null) {
                final Enumeration<String> list = LogManager.getLogManager().getLoggerNames();
                while (list.hasMoreElements()) {
                    initLogger(list.nextElement());
                }
                initLogger(LogCategory.OPENEJB.getName());
            }
        }
    }

    private static void initLogger(final String name) {
        final Logger logger = Logger.getLogger(name);
        final Handler[] handlers = logger.getHandlers();
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                logger.removeHandler(handlers[i]);
            }
        }
        logger.setUseParentHandlers(false);
        logger.addHandler(new JuliLogStreamFactory.OpenEJBSimpleLayoutHandler());
    }

    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        if (ArquillianUtil.isCurrentAdapter(OPENEJB_ADAPTER_NAME)) {
            extensionBuilder.service(DeployableContainer.class, OpenEJBDeployableContainer.class)
                .service(TestEnricher.class, OpenEJBInjectionEnricher.class)
                .service(ApplicationArchiveProcessor.class, OpenEJBArchiveProcessor.class)
                .observer(TestObserver.class);
        }
    }
}
