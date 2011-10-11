package org.superbiz.rest.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rmannibucau
 */
@ApplicationPath("/rest-prefix")
public class ApplicationConfig extends Application {
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(SimpleRESTPojo.class, SimpleRESTEJB.class));
    }
}
