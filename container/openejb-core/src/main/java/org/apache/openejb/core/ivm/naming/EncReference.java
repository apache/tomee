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

import javax.naming.NameNotFoundException;

import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.ThreadContext;


/**
 * This class is a wrapper for an Intra-VM EJB or Connector references in the
 * JNDI ENC of a entity, stateful and stateless beans.  When the getObject( ) method is invoked the
 * Operation is checked to ensure that its is allowed for the bean's current state.
 * <p/>
 * This class is subclassed by EncReference in the entity, stateful and stateless packages
 * of org.apache.openejb.core.
 *
 * @version $Rev$ $Date$
 */
public abstract class EncReference extends Reference {

    protected Reference ref = null;
    protected boolean checking = true;

    public EncReference(Reference ref) {
        this.ref = ref;
    }

    public void setChecking(boolean value) {
        checking = value;
    }

    /**
     * Obtains the referenced object.
     * TODO: I think that once the bean context is properly hooked up
     * throughout the server, then we can remove the check for a null
     * bean context.
     */
    public Object getObject() throws javax.naming.NamingException {
        ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext != null) {
            BaseContext beanContext = callContext.getBeanContext();
            if (beanContext != null) checkOperation(beanContext);
        }
        return ref.getObject();
    }

    public abstract void checkOperation(BaseContext beanContext) throws NameNotFoundException;
}
