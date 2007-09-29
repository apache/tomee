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
package org.apache.openejb.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URI;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ServiceLocator {

    private final Context context;

    public ServiceLocator(URI serverUri) throws NamingException {
        this(serverUri, null, null, null);
    }

    public ServiceLocator(URI serverUri, String username, String password) throws NamingException {
        this(serverUri, username, password, null);
    }

    public ServiceLocator(URI serverUri, String username, String password, String realm) throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.put(Context.PROVIDER_URL, serverUri.toString());
        if (username != null && password != null) {
            properties.put(Context.SECURITY_PRINCIPAL, username);
            properties.put(Context.SECURITY_CREDENTIALS, password);
            if (realm != null) properties.put("openejb.authentication.realmName", realm);
        }
        this.context = new InitialContext(properties);
    }


    public ServiceLocator(Context context) {
        this.context = context;
    }

    public Object lookup(String name) {
        try {
            return context.lookup(name);
        } catch (NamingException e) {
            throw new IllegalArgumentException(e);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
