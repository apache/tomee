package org.apache.openejb.arquillian.tests.getresources;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * jira: TOMEE-42.
 *
 * @author rmannibucau
 */
@WebListener
public class GetResourcesListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent sce) {
        GetResourcesHolder.RESOURCE_NUMBER = 0;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = classLoader.getResources("config/test.getresources2");
            while (urls.hasMoreElements()) {
                urls.nextElement();
                GetResourcesHolder.RESOURCE_NUMBER++;
            }
        } catch (IOException e) {
            // no-op
        }
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
         // no-op
    }
}
