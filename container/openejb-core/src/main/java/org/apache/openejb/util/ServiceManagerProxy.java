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

import org.apache.openejb.core.LocalInitialContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class ServiceManagerProxy {

    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ServiceManagerProxy.class);

    private final Object serviceManager;
    private final Class serviceManagerClass;


    public class AlreadyStartedException extends Exception {
        public AlreadyStartedException(final String s) {
            super(s);
        }
    }

    public ServiceManagerProxy() throws AlreadyStartedException {
        this(true);
    }

    public ServiceManagerProxy(final boolean checkAlreadyStarted) throws AlreadyStartedException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.ServiceManager");
        } catch (final ClassNotFoundException e) {
            final String msg = "Enabling option '" + LocalInitialContext.OPENEJB_EMBEDDED_REMOTABLE + "' requires class 'org.apache.openejb.server.ServiceManager' to be available.  Make sure you have the openejb-server-*.jar in your classpath and at least one protocol implementation such as openejb-ejbd-*.jar.";
            throw new IllegalStateException(msg, e);
        }

        final Method get = getMethod("get");
        final Method getManager = getMethod("getManager");

        if (checkAlreadyStarted && invoke(get, null) != null) {
            throw new AlreadyStartedException("Server services already started");
        }

        serviceManager = invoke(getManager, null);

        logger.info("Initializing network services");

        final Method init = getMethod("init");

        invoke(init, serviceManager);
    }

    private Method getMethod(final String name, final Class... parameterTypes) {
        try {
            return serviceManagerClass.getMethod(name, parameterTypes);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Cannot load the ServiceManager", e);
        }
    }


    private Object invoke(final Method method, final Object obj, final Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Failed executing ServiceManager." + method.getName(), e.getCause());
        }
    }

    public void start() {

        logger.info("Initializing network services");

        final Method start = getMethod("start", boolean.class);

        invoke(start, serviceManager, false);
    }

    public void stop() {
        logger.info("Stopping network services");

        final Method stop = getMethod("stop");

        invoke(stop, serviceManager);
    }
}
