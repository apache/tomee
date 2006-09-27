package org.apache.openejb.server;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.util.FastThreadLocal;

import org.apache.openejb.core.ivm.IntraVmServer;
import org.apache.openejb.spi.ApplicationServer;

import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

public class ServerFederation implements ApplicationServer {

    private static FastThreadLocal threadStorage = new FastThreadLocal();

    public Handle getHandle(ProxyInfo proxyInfo) {
        return getApplicationServer().getHandle(proxyInfo);
    }

    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBMetaData(proxyInfo);
    }

    public HomeHandle getHomeHandle(ProxyInfo proxyInfo) {
        return getApplicationServer().getHomeHandle(proxyInfo);
    }

    public EJBObject getEJBObject(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBObject(proxyInfo);
    }

    public EJBHome getEJBHome(ProxyInfo proxyInfo) {
        return getApplicationServer().getEJBHome(proxyInfo);
    }

    public static void setApplicationServer(ApplicationServer server) {
        if (server != null) {
            threadStorage.set(server);
        }
    }

    public static ApplicationServer getApplicationServer() {
        ApplicationServer server = (ApplicationServer) threadStorage.get();

        return (server == null) ? localServer : server;
    }

    private static final IntraVmServer localServer = new IntraVmServer();
}

