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
package org.apache.openejb.util;

import java.util.LinkedList;
import java.io.Serializable;

import org.apache.openejb.cache.InstanceFactory;
import org.apache.openejb.cache.InstancePool;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class SoftLimitedInstancePool implements InstancePool, Serializable {
    private final InstanceFactory factory;
    private final int maxSize;
    private transient final LinkedList pool;

    public SoftLimitedInstancePool(final InstanceFactory factory, final int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        pool = new LinkedList();
    }

    public Object acquire() throws Exception {
        // get the instance from the pool if possible
        synchronized (this) {
            if (!pool.isEmpty()) {
                return pool.removeFirst();
            }
        }

        // if there was nothing in the pool, we will just create one
        return factory.createInstance();
    }

    public boolean release(Object instance) {
        synchronized (this) {
            // if we are under the limit put it back in the pool at the head
            // this encourages reuse of the same instances to improve memory management
            if (pool.size() < maxSize) {
                pool.addFirst(instance);
                return true;
            }
        }

        // we aren't going to keep this instance, shut it down
        factory.destroyInstance(instance);
        return false;
    }

    public void remove(Object instance) {
        // You broke one, so you get to take the hit and create a replacement
        // Do this outside the synchronized block because the factory can take a long time.
        try {
            instance = factory.createInstance();
        } catch (Exception ignored) {
            // We ignore this as we want the app to see the Exception that
            // caused the instance to be discarded in the first place
            // If the problem is serious, then the next user will see
            // it again when they acquire a new instance
            return;
        }

        // Add the replacement to the pool
        // This may cause us to exceed maxSize, but we'll put it in anyway given
        // we went to the trouble to create it.
        synchronized (this) {
            // add this new instance to the end
            // we prefer other users get older instances first
            pool.addLast(instance);
        }
    }

    private Object readResolve() {
        return new SoftLimitedInstancePool(factory, maxSize);
    }
}

