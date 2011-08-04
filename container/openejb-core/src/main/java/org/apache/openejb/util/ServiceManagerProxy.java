/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.openejb.client.LocalInitialContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class ServiceManagerProxy {

    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, LocalInitialContext.class);

    private final Object serviceManager;
    private final Class serviceManagerClass;


    public class AlreadyStartedException extends Exception {
        public AlreadyStartedException(String s) {
            super(s);
        }
    }

    public ServiceManagerProxy() throws AlreadyStartedException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.ServiceManager");
        } catch (ClassNotFoundException e) {
            String msg = "Enabling option '" + LocalInitialContext.OPENEJB_EMBEDDED_REMOTABLE + "' requires class 'org.apache.openejb.server.ServiceManager' to be available.  Make sure you have the openejb-server-*.jar in your classpath and at least one protocol implementation such as openejb-ejbd-*.jar.";
            throw new IllegalStateException(msg, e);
        }

        Method get = getMethod("get");
        Method getManager = getMethod("getManager");

        if (invoke(get, null) != null) throw new AlreadyStartedException("Server services already started");

        serviceManager = invoke(getManager, null);

        logger.info("Initializing network services");

        Method init = getMethod("init");

        invoke(init, serviceManager);
    }

    private Method getMethod(String name, Class... parameterTypes) {
        try {
            return serviceManagerClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot load the ServiceManager", e);
        }
    }


    private Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Failed executing ServiceManager." + method.getName(), e.getCause());
        }
    }

    public void start() {

        logger.info("Initializing network services");

        Method start = getMethod("start", boolean.class);

        invoke(start, serviceManager, false);
    }

    public void stop() {
        logger.info("Stopping network services");

        Method stop = getMethod("stop");

        invoke(stop, serviceManager);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}
