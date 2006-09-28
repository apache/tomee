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
package org.apache.openejb.entity;

import java.io.Serializable;
import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.cache.InstanceFactory;


/**
 * Factory for EntityInstanceContexts.
 * This factory will create a bean instance, perform initialization by calling
 * setEntityContext and then wrap the instance in a BMPInstanceContext
 * ready for insertion into a pool or use by an Invocation.
 *
 * @version $Revision$ $Date$
 */
public class EntityInstanceFactory implements InstanceFactory, Serializable {
    private static final long serialVersionUID = -7127497820859468692L;
    private static final Log log = LogFactory.getLog(EntityInstanceFactory.class);
    private final InstanceContextFactory factory;

    public EntityInstanceFactory(InstanceContextFactory factory) {
        this.factory = factory;
    }

    public Object createInstance() throws Exception {
        Context oldContext = RootContext.getComponentContext();

        try {
            // Disassociate from JNDI Component Context whilst creating instance
            RootContext.setComponentContext(null);

            // create an EJBInstanceContext wrapping the raw instance
            EntityInstanceContext ctx = (EntityInstanceContext) factory.newInstance();

            try {
                ctx.setContext();
            } catch (Throwable t) {
                ctx.die();
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new Error(t);
                }
            }

            return ctx;
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }

    public void destroyInstance(Object instance) {
        EntityInstanceContext ctx = (EntityInstanceContext) instance;
        try {
            ctx.unsetContext();
        } catch (Throwable t) {
            log.warn("Unexpected error destroying Entity instance", t);
        }
    }
}
