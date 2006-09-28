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
