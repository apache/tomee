package org.apache.openejb.arquillian.tests.listenerremote;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class RemoteServletContextListener implements ServletContextListener {

    @EJB
    private CompanyRemote remoteCompany;

    public void contextInitialized(ServletContextEvent event) {
        final String name = "OpenEJB";
        final ServletContext context = event.getServletContext();

        if (remoteCompany != null) {
            context.setAttribute(ContextAttributeName.KEY_Remote.name(), "Remote: " + remoteCompany.employ(name));
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}