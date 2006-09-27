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
package org.apache.openejb.entity;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.apache.openejb.AbstractInstanceContext;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EntityEjbContainer;
import org.apache.openejb.EntityEjbDeployment;
import org.apache.openejb.cache.InstancePool;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.transaction.EjbTransactionContext;

/**
 * @version $Revision$ $Date$
 */
public abstract class EntityInstanceContext extends AbstractInstanceContext {
    private final EntityEjbContainer entityEjbContainer;
    private Object id;
    private boolean loaded = false;
    private InstancePool pool;
    private EjbTransactionContext ejbTransactionContext;
    private final EntityContextImpl entityContext;

    public EntityInstanceContext(EntityEjbDeployment entityEjbDeployment,
            EntityEjbContainer entityEjbContainer,
            EntityBean instance,
            EJBProxyFactory proxyFactory) {
        super(entityEjbDeployment, instance, proxyFactory);
        this.entityEjbContainer = entityEjbContainer;

        entityContext = new EntityContextImpl(this, entityEjbContainer.getTransactionManager());
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public InstancePool getPool() {
        return pool;
    }

    public void setPool(InstancePool pool) {
        this.pool = pool;
    }

    public void setOperation(EJBOperation operation) {
        entityContext.setState(operation);
    }

    public boolean setTimerState(EJBOperation operation) {
        return entityContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return entityContext;
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    public EjbTransactionContext getEjbTransactionData() {
        return ejbTransactionContext;
    }

    public void setEjbTransactionData(EjbTransactionContext ejbTransactionContext) {
        this.ejbTransactionContext = ejbTransactionContext;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void die() {
        if (pool != null) {
            pool.remove(this);
            pool = null;
        }
        loaded = false;
        setEjbTransactionData(null);
        super.die();
    }

    public void associate() throws Throwable {
        super.associate();
        if (id != null && !loaded) {
            ejbActivate();
            ejbLoad();
            loaded = true;
        }
    }

    public void unassociate() throws Throwable {
        super.unassociate();
        try {
            if (!isDead()) {
                if (id != null) {
                    ejbPassivate();
                }
                if (pool != null) {
                    pool.release(this);
                }
            }
        } catch (Throwable t) {
            // problem passivating instance - discard it and throw the problem (will cause rollback)
            if (pool != null) {
                pool.remove(this);
            }
            throw t;
        } finally {
            loaded = false;
            ejbTransactionContext = null;
        }
    }

    public void beforeCommit() throws Throwable {
        super.beforeCommit();
        flush();
    }

    public void flush() throws Throwable {
        super.flush();
        if (id != null) {
            if (!loaded) {
                throw new IllegalStateException("Trying to invoke ejbStore on an unloaded instance");
            }
            ejbStore();
        }
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        entityEjbContainer.setContext(this, entityContext);
    }

    public void unsetContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        entityEjbContainer.unsetContext(this);
    }

    protected void ejbActivate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        entityEjbContainer.ejbActivate(this);

    }

    protected void ejbPassivate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        entityEjbContainer.ejbPassivate(this);
    }

    protected void ejbLoad() throws Throwable {
        entityEjbContainer.load(this, ejbTransactionContext);
    }

    public void ejbStore() throws Throwable {
        entityEjbContainer.store(this, ejbTransactionContext);
    }

    public void afterCommit(boolean status) throws Throwable {
        super.afterCommit(status);
        ejbTransactionContext = null;
    }
}
