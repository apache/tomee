package org.openejb.spi;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.openejb.ProxyInfo;

public interface ApplicationServer {

    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo);

    public Handle getHandle(ProxyInfo proxyInfo);

    public HomeHandle getHomeHandle(ProxyInfo proxyInfo);

    public EJBObject getEJBObject(ProxyInfo proxyInfo);

    public EJBHome getEJBHome(ProxyInfo proxyInfo);

}