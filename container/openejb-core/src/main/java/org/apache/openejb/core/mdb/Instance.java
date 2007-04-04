package org.apache.openejb.core.mdb;


import java.util.Collections;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Instance {
    public final Object bean;
    public final Map<String,Object> interceptors;

    public Instance(Object bean, Map<String, Object> interceptors) {
        this.bean = bean;
        this.interceptors = interceptors;
    }
}
