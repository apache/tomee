package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.connector.Request;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;

/**
 * @author rmannibucau
 */
public class OpenEJBSecurityListener implements AsyncListener {
    private TomcatSecurityService securityService;
    private Object oldState = null;
    private Request request;

    public OpenEJBSecurityListener(TomcatSecurityService service, Request req) {
        securityService = service;
        request = req;
    }

    @Override public void onComplete(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    @Override public void onError(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    @Override public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        enter();
    }

    @Override public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    public void enter() {
        if (securityService != null && request.getWrapper() != null) {
            oldState = securityService.enterWebApp(request.getWrapper().getRealm(), request.getPrincipal(), request.getWrapper().getRunAs());
        }
    }

    public void exit() {
        if (securityService != null) {
            securityService.exitWebApp(oldState);
        }
    }
}
