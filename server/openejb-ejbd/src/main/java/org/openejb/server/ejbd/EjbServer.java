package org.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.openejb.util.Messages;
import org.openejb.util.Logger;
import org.openejb.server.ServiceException;
import org.openejb.server.ServerFederation;
import org.openejb.client.RequestMethods;
import org.openejb.ProxyInfo;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

public class EjbServer implements org.openejb.server.ServerService, org.openejb.spi.ApplicationServer {

    EjbDaemon server;

    public void init(Properties props) throws Exception {
        server = EjbDaemon.getEjbDaemon();
        server.init(props);
    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "ejbd";
    }

    public int getPort() {
        return 0;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        ServerFederation.setApplicationServer(server);
        server.service(socket);
    }

    public void service(InputStream inputStream, OutputStream outputStream) throws ServiceException, IOException {
        server.service(inputStream, outputStream);
    }

    public String getIP() {
        return "";
    }

    public EJBMetaData getEJBMetaData(ProxyInfo info) {
        return server.getEJBMetaData(info);
    }

    public Handle getHandle(ProxyInfo info) {
        return server.getHandle(info);
    }

    public HomeHandle getHomeHandle(ProxyInfo info) {
        return server.getHomeHandle(info);
    }

    public EJBObject getEJBObject(ProxyInfo info) {
        return server.getEJBObject(info);
    }

    public EJBHome getEJBHome(ProxyInfo info) {
        return server.getEJBHome(info);
    }
}
