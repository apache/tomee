package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.HttpListener;

import javax.ws.rs.core.Application;

/**
 * @author Romain Manni-Bucau
 */
public interface RsHttpListener extends HttpListener {
    public static enum Scope {
        SINGLETON, PROTOTYPE
    }

    void deploy(String address, Object o, Application app);
    void undeploy();
}
