/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.entity;

import java.io.Serializable;
import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.java.RootContext;
import org.openejb.InstanceContextFactory;
import org.openejb.cache.InstanceFactory;


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
