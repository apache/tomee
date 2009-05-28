/**
 *
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
package org.apache.openejb.client;

import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.OptionsLog;

import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    private static OpenEJBInstance openejb;

    private boolean bootedOpenEJB;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        init(env);
        return getLocalInitialContext(env);
    }

    private void init(Hashtable env) throws javax.naming.NamingException {
        if (openejb != null) {
            return;
        }
        try {
            Properties properties = new Properties();
            properties.putAll(env);
            init(properties);
        } catch (Exception e) {
            throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
        }
    }

    boolean bootedOpenEJB() {
        return bootedOpenEJB;
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        bootedOpenEJB = true;
        SystemInstance.init(properties);
        OptionsLog.install();
        SystemInstance.get().setProperty("openejb.embedded", "true");
        openejb.init(properties);
    }

    public void close(){
        openejb = null;
    }

    private Context getLocalInitialContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            ClassLoader cl = SystemInstance.get().getClassLoader();

            Class localInitialContext = Class.forName("org.apache.openejb.client.LocalInitialContext", true, cl);

            Constructor constructor = localInitialContext.getConstructor(Hashtable.class, this.getClass());
            context = (Context) constructor.newInstance(env, this);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e;
                if (ite.getTargetException() != null){
                    e = ite.getTargetException();
                }
            }

            if (e instanceof NamingException){
                throw (NamingException) e;
            }
            throw (NamingException) new javax.naming.NamingException("Cannot instantiate a LocalInitialContext. Exception: "
                    + e.getClass().getName() + " " + e.getMessage()).initCause(e);
        }

        return context;
    }

}