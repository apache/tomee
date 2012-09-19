package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;

import java.io.File;
import java.net.MalformedURLException;

public class AdditionalLibClassLoaderEnricherObserver {
    public static final String OPENEJB_ENRICHER_ADDITIONAL_LIB = "openejb.enricher.additional-lib";

    private static boolean initDone = false;

    private String path = "additional-lib";

    public void initEnricher(@Observes final ObserverAdded event) {
        if (initDone || path == null || !AdditionalLibClassLoaderEnricherObserver.class.isInstance(event.getObserver())) {
            return;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            final String systProp = SystemInstance.get().getProperty(OPENEJB_ENRICHER_ADDITIONAL_LIB, (String) null);
            if (systProp != null) {
                dir = new File(systProp);
            }
        }
        if (dir.exists()) {
            final File[] libs = dir.listFiles();
            if (libs != null) {
                final ClassLoaderEnricher enricher = SystemInstance.get().getComponent(ClassLoaderEnricher.class);
                for (File lib : libs) {
                    try {
                        enricher.addUrl(lib.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new OpenEJBRuntimeException(e);
                    }
                }
            }
        }

        initDone = true;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
