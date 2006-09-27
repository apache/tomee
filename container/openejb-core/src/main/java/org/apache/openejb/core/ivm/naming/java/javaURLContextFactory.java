package org.apache.openejb.core.ivm.naming.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;

public class javaURLContextFactory implements ObjectFactory, InitialContextFactory {

    public Context getInitialContext(Hashtable env) throws NamingException {
        return getContext();
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env)
            throws NamingException {
        if (obj == null) {
            /*
                  A null obj ref means the NamingManager is requesting
                  a Context that can resolve the 'java:' schema
               */
            return getContext();
        } else if (obj instanceof java.lang.String) {
            String string = (String) obj;
            if (string.startsWith("java:comp") || string.startsWith("java:openejb")) {
                /*
                     If the obj is a URL String with the 'java:' schema
                     resolve the URL in the context of this threads JNDI ENC
                     */
                string = string.substring(string.indexOf(':'));
                Context encRoot = getContext();
                return encRoot.lookup(string);
            }
        }
        return null;
    }

    public Object getObjectInstance(Object obj, Hashtable env)
            throws NamingException {
        return getContext();
    }

    public Context getContext() {
        Context jndiCtx = null;

        if (!ThreadContext.isValid()) {
            ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
            return containerSystem.getJNDIContext();
        }

        CoreDeploymentInfo di = ThreadContext.getThreadContext().getDeploymentInfo();
        if (di != null) {
            return di.getJndiEnc();
        } else {
            ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
            return containerSystem.getJNDIContext();
        }
    }
}
