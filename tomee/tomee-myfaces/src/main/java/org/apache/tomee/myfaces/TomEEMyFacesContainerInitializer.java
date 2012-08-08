package org.apache.tomee.myfaces;

import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.ee6.MyFacesContainerInitializer;
import org.apache.myfaces.spi.FacesConfigResourceProvider;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;

import javax.faces.context.ExternalContext;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class TomEEMyFacesContainerInitializer implements ServletContainerInitializer {
    private final MyFacesContainerInitializer delegate;

    public TomEEMyFacesContainerInitializer() {
        delegate = new MyFacesContainerInitializer();
    }

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        // try to skip first
        if ("true".equalsIgnoreCase(ctx.getInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE"))) {
            return;
        }
        if ((classes != null && !classes.isEmpty()) || isFacesConfigPresent(ctx)) {
            // we found a faces-config.xml or some classes so let's delegate to myfaces
            delegate.onStartup(classes, ctx);
        }
    }

    // that's the reason why we fork: we don't want to consider our internal faces-config.xml
    // see delegate for details
    private boolean isFacesConfigPresent(ServletContext servletContext) {
        try {
            if (servletContext.getResource("/WEB-INF/faces-config.xml") != null) {
                return true;
            }

            final String configFilesAttrValue = servletContext.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
            if (configFilesAttrValue != null) {
                String[] configFiles = configFilesAttrValue.split(",");
                for (String file : configFiles) {
                    if (servletContext.getResource(file.trim()) != null) {
                        return true;
                    }
                }
            }

            final ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, true);
            final FacesConfigResourceProviderFactory factory = FacesConfigResourceProviderFactory.
                    getFacesConfigResourceProviderFactory(externalContext);
            final FacesConfigResourceProvider provider = factory.createFacesConfigResourceProvider(externalContext);
            final Collection<URL> metaInfFacesConfigUrls =  provider.getMetaInfConfigurationResources(externalContext);

            // remove our internal faces-config.xml
            final Iterator<URL> it = metaInfFacesConfigUrls.iterator();
            while (it.hasNext()) {
                if (it.next().toExternalForm().replace(File.separator, "/").contains("lib/openwebbeans-jsf-")) {
                    it.remove();
                }
            }

            return metaInfFacesConfigUrls != null && !metaInfFacesConfigUrls.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

}
