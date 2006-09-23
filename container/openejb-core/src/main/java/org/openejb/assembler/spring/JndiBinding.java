/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.assembler.spring;

import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;

import org.springframework.beans.factory.FactoryBean;

/**
 * @org.apache.xbean.XBean element="jndiBinding"
 * @version $Revision$ $Date$
 */
public class JndiBinding implements FactoryBean {
    private Context context;
    private Map<String, Object> bindings;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    /**
     * @org.apache.xbean.InitMethod
     */
    public void start() throws NamingException {
        if (context == null && bindings != null) {
            throw new NullPointerException("Naming context has not been set");
        }
        if (bindings == null) {
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                context.bind(name, value);
            }
        } catch (NamingException e) {
            stop();
            throw e;
        }
    }

    /**
     * @org.apache.xbean.DestroyMethod
     */
    public void stop() {
        if (context == null) {
            return;
        }
        if (bindings == null) {
            return;
        }
        for (String name : bindings.keySet()) {
            try {
                context.unbind(name);
            } catch (NamingException ignored) {
            }
        }
    }

    public Object getObject() throws Exception {
        return context;
    }

    public Class getObjectType() {
        return Context.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
