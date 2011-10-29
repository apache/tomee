package org.apache.openejb.arquillian.tests.listenerremote;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


public class RemoteServletSessionListener implements HttpSessionListener {

    @EJB
    private CompanyRemote remoteCompany;

    public void sessionCreated(HttpSessionEvent event) {
        final String name = "OpenEJB";
        final HttpSession context = event.getSession();

        if (remoteCompany != null) {
            context.setAttribute(ContextAttributeName.KEY_Remote.name(), "Remote: " + remoteCompany.employ(name));
        }
    }

    public void sessionDestroyed(HttpSessionEvent event) {
    }

}