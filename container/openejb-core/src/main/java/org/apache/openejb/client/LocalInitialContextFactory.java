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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.Properties;

public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    static Context intraVmContext;
    private static OpenEJBInstance openejb;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (intraVmContext == null) {
            try {
                Properties properties = new Properties();
                properties.putAll(env);
                init(properties);
            } catch (Exception e) {
                throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
            }
            intraVmContext = getIntraVmContext(env);
        }
        return intraVmContext;
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        SystemInstance.init(properties);
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        openejb.init(properties);
    }

    private Context getIntraVmContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            InitialContextFactory factory = null;
            ClassLoader cl = SystemInstance.get().getClassLoader();
            Class ivmFactoryClass = Class.forName("org.apache.openejb.core.ivm.naming.InitContextFactory", true, cl);

            factory = (InitialContextFactory) ivmFactoryClass.newInstance();
            context = factory.getInitialContext(env);
        } catch (Exception e) {
            throw new javax.naming.NamingException("Cannot instantiate an IntraVM InitialContext. Exception: "
                    + e.getClass().getName() + " " + e.getMessage());
        }

        return context;
    }
}