package org.apache.tomee.myfaces;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TomEEMyFacesContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        // no-op
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        TomEEFacesConfigResourceProvider.clear(sce.getServletContext().getClassLoader());
    }
}
