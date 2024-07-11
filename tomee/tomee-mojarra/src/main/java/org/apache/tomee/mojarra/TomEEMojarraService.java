package org.apache.tomee.mojarra;

import com.sun.faces.cdi.CdiExtension;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.spi.Service;
import org.apache.tomee.mojarra.owb.OwbCompatibleCdiExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TomEEMojarraService implements Service {
    @Override
    public void init(Properties props) throws Exception {
        SystemInstance.get().addObserver(this);
    }

    public void beforeDeployment(@Observes BeforeDeploymentEvent event) {
        Map<String, String> replacements = OptimizedLoaderService.EXTENSION_REPLACEMENTS.get();
        if (replacements == null) {
            replacements = new HashMap<>();
            OptimizedLoaderService.EXTENSION_REPLACEMENTS.set(replacements);
        }

        replacements.put(CdiExtension.class.getName(), OwbCompatibleCdiExtension.class.getName());
    }
}
