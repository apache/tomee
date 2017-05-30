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

package org.apache.openejb.core;

import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.OptionsLog;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
@Deprecated // use org.apache.openejb.core.OpenEJBInitialContextFactory
public class LocalInitialContextFactory implements InitialContextFactory {

    private static final ReentrantLock lock = new ReentrantLock();
    private static OpenEJBInstance openejb;

    private boolean bootedOpenEJB;

    @Override
    public Context getInitialContext(final Hashtable env) throws NamingException {
        init(env);
        return getLocalInitialContext(env);
    }

    protected void init(final Hashtable env) throws NamingException {

        final ReentrantLock l = lock;
        l.lock();

        try {
            if (openejb != null && openejb.isInitialized()) {
                return;
            }
            try {
                final Properties properties = new Properties();
                properties.putAll(env);
                init(properties);
            } catch (final Exception e) {
                throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
            }
        } finally {
            l.unlock();
        }
    }

    boolean bootedOpenEJB() {
        final ReentrantLock l = lock;
        l.lock();

        try {
            return bootedOpenEJB;
        } finally {
            l.unlock();
        }

    }

    private void init(final Properties properties) throws Exception {
        if (openejb != null && openejb.isInitialized()) {
            return;
        }

        openejb = new OpenEJBInstance();

        if (openejb.isInitialized()) {
            return;
        }

        bootedOpenEJB = true;
        SystemInstance.init(properties);
        OptionsLog.install();
        SystemInstance.get().setProperty("openejb.embedded", "true");
        openejb.init(properties);
    }

    public void close() {
        final ReentrantLock l = lock;
        l.lock();

        try {
            openejb = null;
        } finally {
            l.unlock();
        }
    }

    private Context getLocalInitialContext(final Hashtable env) throws NamingException {
        final Context context;
        try {
            final ClassLoader cl = SystemInstance.get().getClassLoader();

            final Class localInitialContext = Class.forName("org.apache.openejb.core.LocalInitialContext", true, cl);

            //noinspection unchecked
            final Constructor constructor = localInitialContext.getConstructor(Hashtable.class, LocalInitialContextFactory.class);
            context = (Context) constructor.newInstance(env, this);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                final InvocationTargetException ite = (InvocationTargetException) e;
                if (ite.getTargetException() != null) {
                    e = ite.getTargetException();
                }
            }

            if (e instanceof NamingException) {
                throw (NamingException) e;
            }
            throw (NamingException) new NamingException("Cannot instantiate a LocalInitialContext. Exception: "
                + e.getClass().getName() + " " + e.getMessage()).initCause(e);
        }

        return context;
    }

}