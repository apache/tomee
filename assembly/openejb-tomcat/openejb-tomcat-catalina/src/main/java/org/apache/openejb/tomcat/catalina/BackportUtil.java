/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ContextService;
import org.apache.catalina.deploy.NamingResources;
import org.apache.openejb.tomcat.common.TomcatVersion;

import javax.servlet.Servlet;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @version $Rev$ $Date$
 */
public class BackportUtil {

    private static API api;

    static {
        if (TomcatVersion.v6.isTheVersion()) {
            api = new Tomcat6();
        } else {
            api = new Tomcat55();
        }
    }

    private static API getAPI() {
        return api;
    }

    public static Servlet getServlet(Wrapper wrapper) {
        return getAPI().getServlet(wrapper);
    }

    public static NamingContextListener getNamingContextListener(StandardContext standardContext) {
        return getAPI().getNamingContextListener(standardContext);
    }

    public static String findServiceName(NamingResources naming, String refName) {
        return getAPI().findServiceName(naming, refName);
    }

    public static void removeService(NamingContextListener namingContextListener, String serviceName) {
        getAPI().removeService(namingContextListener, serviceName);
    }

    public static interface API {
        Servlet getServlet(Wrapper wrapper);

        String findServiceName(NamingResources naming, String refName);

        NamingContextListener getNamingContextListener(StandardContext standardContext);

        void removeService(NamingContextListener namingContextListener, String serviceName);
    }


    public static class Tomcat6 implements API {
        public Servlet getServlet(Wrapper wrapper) {
            return wrapper.getServlet();
        }

        public NamingContextListener getNamingContextListener(StandardContext standardContext) {
            return standardContext.getNamingContextListener();
        }

        public String findServiceName(NamingResources naming, String referenceName) {
            ContextService service = naming.findService(referenceName);
            return (service != null) ? service.getName() : null;
        }

        public void removeService(NamingContextListener namingContextListener, String serviceName) {
            namingContextListener.removeService(serviceName);
        }
    }

    public static class Tomcat55 implements API {
        private final Field namingContextListener;
        private final Field instance;

        public Tomcat55() {
            namingContextListener = getField(StandardContext.class, "namingContextListener");
            instance = getField(StandardWrapper.class, "instance");
        }


        public Servlet getServlet(Wrapper wrapper) {
            try {
                return (Servlet) instance.get(wrapper);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public NamingContextListener getNamingContextListener(StandardContext standardContext) {
            try {
                return (NamingContextListener) namingContextListener.get(standardContext);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public String findServiceName(NamingResources naming, String refName) {
            return null;
        }

        public void removeService(NamingContextListener namingContextListener, String serviceName) {
        }

        private Field getField(final Class clazz, final String name) {
            return AccessController.doPrivileged(new PrivilegedAction<Field>() {
                public Field run() {
                    try {
                        Field field = clazz.getDeclaredField(name);
                        field.setAccessible(true);
                        return field;
                    } catch (Exception e2) {
                        throw (IllegalStateException) new IllegalStateException("Unable to find or access the '" + name + "' field in " + clazz.getName()).initCause(e2);
                    }
                }
            });
        }

    }


}
