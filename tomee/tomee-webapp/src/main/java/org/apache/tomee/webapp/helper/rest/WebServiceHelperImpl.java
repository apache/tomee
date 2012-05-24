package org.apache.tomee.webapp.helper.rest;

import java.util.List;
import java.util.Map;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.SimpleServiceManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class WebServiceHelperImpl {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, WebServiceHelperImpl.class);

    private WebServiceHelperImpl() {
        // no-op
    }

    // TODO
    // it should be done in tomee-plus and/or tomee-jaxrs
    // because in these webapp we can use our internal API
    // but to keep the webapp(s) dev consistent
    // we put it here and use reflection.
    // a better solution is to add a kind of spi for our webapps
    // to be able to add for each overlay new graphic part
    public static Services restWebServices() {
        final Services restServices = new Services();

        final ServerService ss = serverService("org.apache.openejb.server.cxf.rs.CxfRSService");
        if (ss == null) {
            return restServices;
        }

        try {
            final List<Object> services = (List<Object>) ss.getClass().getMethod("getServices").invoke(ss);
            for (Object rsService : services) {
                final Accessor accessor = new Accessor(rsService);
                final String app = (String) accessor.value("webapp");
                final String address = (String) accessor.value("address");
                final String origin = (String) accessor.value("origin");
                restServices.returnOrCreateApplication(app).getServices().add(new Service(origin, address + "?wadl"));
            }
        } catch (Exception e) {
            LOGGER.warning("can't get rest services", e);
            return restServices;
        }

        return restServices;
    }

    public static Services soapWebServices() {
        final Services soapServices = new Services();

        final ServerService ss = serverService("org.apache.openejb.server.cxf.CxfService");
        if (ss == null) {
            return soapServices;
        }

        try {
            final Map<String, List<Object>> addressesByApp = (Map) ss.getClass().getMethod("getAddressesByApplication").invoke(ss);
            for (Map.Entry<String, List<Object>> entry : addressesByApp.entrySet()) {
                final Application app = soapServices.returnOrCreateApplication(entry.getKey());
                for (Object soapService : entry.getValue()) {
                    final Accessor accessor = new Accessor(soapService);
                    final String wsdl = (String) accessor.value("address");
                    final String port = (String) accessor.value("portName");
                    final String classname = (String) accessor.value("classname");
                    app.getServices().add(new SoapService(classname, wsdl + "?wsdl",port));
                }
            }
        } catch (Exception e) {
            LOGGER.warning("can't get soap services", e);
            return soapServices;
        }

        return soapServices;
    }

    private static ServerService serverService(final String clazz) {
        final ServiceManager sm = ServiceManager.get();
        if (!(sm instanceof SimpleServiceManager)) { // we don't know
            LOGGER.warning("the service manager used is not a simple service manager so rest services can't be retrieved");
            return null;
        }

        final SimpleServiceManager ssm = (SimpleServiceManager) sm;
        final ServerService[] serverServices = ssm.getDaemons();
        if (serverServices == null) {
            LOGGER.warning("no service started");
            return null;
        }

        for (ServerService ss : serverServices) {
            if (clazz.equals(ss.getClass().getName())) {
                return ss;
            }
        }

        return null;
    }

    private static class Accessor {
        private Object o;

        public Accessor(Object o) {
            this.o = o;
        }

        public Object value(final String attr) {
            try {
                return o.getClass().getField(attr).get(o);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
