package org.openejb.core.ivm;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.openejb.ProxyInfo;
import org.openejb.core.entity.EntityEjbHomeHandler;
import org.openejb.core.stateful.StatefulEjbHomeHandler;
import org.openejb.core.stateless.StatelessEjbHomeHandler;
import org.openejb.util.proxy.ProxyManager;

public class IntraVmServer implements org.openejb.spi.ApplicationServer {

    public EJBMetaData getEJBMetaData(ProxyInfo pi) {
        org.openejb.DeploymentInfo di = pi.getDeploymentInfo();
        IntraVmMetaData metaData = new IntraVmMetaData(di.getHomeInterface(), di.getRemoteInterface(), di.getComponentType());

        metaData.setEJBHome(getEJBHome(pi));
        return metaData;
    }

    public Handle getHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBObject(pi));
    }

    public HomeHandle getHomeHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBHome(pi));
    }

    public EJBObject getEJBObject(ProxyInfo pi) {
        EjbHomeProxyHandler handler = null;
        return (EJBObject) getEjbHomeHandler(pi).createProxy(pi);
    }

    public EJBHome getEJBHome(ProxyInfo pi) {

        if (pi.getDeploymentInfo() instanceof org.openejb.core.CoreDeploymentInfo) {
            org.openejb.core.CoreDeploymentInfo coreDeployment = (org.openejb.core.CoreDeploymentInfo) pi.getDeploymentInfo();
            return coreDeployment.getEJBHome();

        } else {
            try {
                Class[] interfaces = new Class[]{pi.getDeploymentInfo().getHomeInterface(), org.openejb.core.ivm.IntraVmProxy.class};
                return (javax.ejb.EJBHome) ProxyManager.newProxyInstance(interfaces, getEjbHomeHandler(pi));
            } catch (Exception e) {
                throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
            }
        }
    }

    private EjbHomeProxyHandler getEjbHomeHandler(ProxyInfo pi) {

        switch (pi.getDeploymentInfo().getComponentType()) {

            case org.openejb.DeploymentInfo.BMP_ENTITY:
            case org.openejb.DeploymentInfo.CMP_ENTITY:
                return new EntityEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());

            case org.openejb.DeploymentInfo.STATEFUL:
                return new StatefulEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());

            case org.openejb.DeploymentInfo.STATELESS:
                return new StatelessEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());
            default:
                throw new RuntimeException("Unknown EJB type: " + pi.getDeploymentInfo());
        }
    }
}
