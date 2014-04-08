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
package org.apache.openejb.client.java;

import org.apache.openejb.client.RemoteInitialContextFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class javaURLContextFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        final RemoteInitialContextFactory context = new RemoteInitialContextFactory();

        final String serverUri = System.getProperty("openejb.server.uri");
        final String moduleId = System.getProperty("openejb.client.moduleId");

        final Properties props = new Properties();
        props.setProperty(Context.PROVIDER_URL, serverUri);
        props.setProperty("openejb.client.moduleId", moduleId);

        return context.getInitialContext(props);
    }
}
