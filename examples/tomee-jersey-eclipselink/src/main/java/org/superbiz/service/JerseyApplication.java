package org.superbiz.service;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class JerseyApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(PersonService.class);
        return classes;
    }
}
