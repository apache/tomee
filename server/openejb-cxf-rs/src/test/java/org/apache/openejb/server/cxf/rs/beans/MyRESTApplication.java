package org.apache.openejb.server.cxf.rs.beans;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Romain Manni-Bucau
 */
public class MyRESTApplication extends Application {
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(MyExpertRestClass.class));
    }

    public Set<Object> getSingletons() {
        return new HashSet<Object>(Arrays.asList(new MyFirstRestClass(), new MySecondRestClass()));
    }
}
