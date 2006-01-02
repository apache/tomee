package org.openejb.client;

import org.openejb.loader.OpenEJBInstance;
import org.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.Properties;

public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    static Context intraVmContext;
    private static OpenEJBInstance openejb;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (intraVmContext == null) {
            try {
                Properties properties = new Properties();
                properties.putAll(env);
                init(properties);
            } catch (Exception e) {
                throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
            }
            intraVmContext = getIntraVmContext(env);
        }
        return intraVmContext;
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        SystemInstance.init(properties);
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        openejb.init(properties);
    }

    private Context getIntraVmContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            InitialContextFactory factory = null;
            ClassLoader cl = SystemInstance.get().getClassLoader();
            Class ivmFactoryClass = Class.forName("org.openejb.core.ivm.naming.InitContextFactory", true, cl);

            factory = (InitialContextFactory) ivmFactoryClass.newInstance();
            context = factory.getInitialContext(env);
        } catch (Exception e) {
            throw new javax.naming.NamingException("Cannot instantiate an IntraVM InitialContext. Exception: "
                    + e.getClass().getName() + " " + e.getMessage());
        }

        return context;
    }
}

