package org.apache.tomee.webapp.helper.rest;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.SimpleServiceManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.Field;
import java.util.List;

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
    public static RestServices restWebServices() {
        final RestServices restServices = new RestServices();

        final ServiceManager sm = ServiceManager.get();
        if (!(sm instanceof SimpleServiceManager)) { // we don't know
            LOGGER.warning("the service manager used is not a simple service manager so rest services can't be retrieved");
            return restServices;
        }

        final SimpleServiceManager ssm = (SimpleServiceManager) sm;
        final ServerService[] serverServices = ssm.getDaemons();
        if (serverServices == null) {
            LOGGER.warning("no service started");
            return restServices;
        }

        for (ServerService ss : serverServices) {
            if ("org.apache.openejb.server.rest.RESTService".equals(ss.getClass().getName())) {
                try {
                    final List<Object> services = (List<Object>) ss.getClass().getMethod("getServices").invoke(ss);
                    if (services.isEmpty()) {
                        return restServices;
                    }

                    final Class<?> infoClass = services.iterator().next().getClass();
                    final Field appNameAccessor = infoClass.getField("webapp");
                    final Field addressAccessor = infoClass.getField("address");
                    final Field originAccessor = infoClass.getField("origin");

                    for (Object rsService : services) {
                        final String app = (String) appNameAccessor.get(rsService);
                        final String address = (String) addressAccessor.get(rsService);
                        final String origin = (String) originAccessor.get(rsService);
                        restServices.returnOrCreateApplication(app).getServices().add(new RestService(origin, address));
                    }
                } catch (Exception e) {
                    LOGGER.warning("can't get rest services", e);
                    return restServices;
                }
                break;
            }
        }

        return restServices;
    }
}
