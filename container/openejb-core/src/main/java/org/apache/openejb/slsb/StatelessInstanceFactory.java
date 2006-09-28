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
package org.apache.openejb.slsb;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.cache.InstanceFactory;


/**
 * @version $Revision$ $Date$
 */
public class StatelessInstanceFactory implements InstanceFactory, Serializable {
    private static final long serialVersionUID = 4525294526104231811L;
    private static final Log log = LogFactory.getLog(StatelessInstanceFactory.class);

    private final InstanceContextFactory factory;

    public StatelessInstanceFactory(InstanceContextFactory factory) {
        this.factory = factory;
    }

    public Object createInstance() throws Exception {
        StatelessInstanceContext ctx = (StatelessInstanceContext) factory.newInstance();
        try {
            ctx.setContext();
            ctx.ejbCreate();
            return ctx;
        } catch (Throwable t) {
            if (t instanceof Exception) {
                throw (Exception) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error("Unexpected throwable", t);
            }
        }
    }

    public void destroyInstance(Object instance) {
        StatelessInstanceContext ctx = (StatelessInstanceContext) instance;
        try {
            ctx.ejbRemove();
        } catch (Throwable t) {
            // We're destroying this instance, so just log and continue
            log.warn("Unexpected error removing Stateless instance", t);
        }
        // No should not we call setSessionContext(null);
    }

    protected Object readResolve() {
        return new StatelessInstanceFactory(factory);
    }
}

