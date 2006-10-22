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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;

import org.apache.openejb.EnvProps;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class InitContextFactory implements javax.naming.spi.InitialContextFactory {

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (!org.apache.openejb.OpenEJB.isInitialized()) {
            initializeOpenEJB(env);
        }

        ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        Context context = containerSystem.getJNDIContext();
        context = (Context) context.lookup("java:openejb/ejb");
        return context;

    }

    private void initializeOpenEJB(Hashtable env) throws javax.naming.NamingException {
        try {
            Properties props = new Properties();

            /* DMB: We should get the defaults from the functionality
            *      Alan is working on.  This is temporary.
            *      When that logic is finished, this block should
            *      probably just be deleted.
            */
            props.put(EnvProps.ASSEMBLER, "org.apache.openejb.assembler.classic.Assembler");
            props.put(EnvProps.CONFIGURATION_FACTORY, "org.apache.openejb.alt.config.ConfigurationFactory");
            props.put(EnvProps.CONFIGURATION, "conf/default.openejb.conf");

            props.putAll(SystemInstance.get().getProperties());

            props.putAll(env);

            org.apache.openejb.OpenEJB.init(props);

        }
        catch (org.apache.openejb.OpenEJBException e) {
            throw new NamingException("Cannot initailize OpenEJB", e);
        }
        catch (Exception e) {
            throw new NamingException("Cannot initailize OpenEJB", e);
        }
    }

}

