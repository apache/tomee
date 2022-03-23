/*
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

package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.TransactionManagerWrapper;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.xa.ManagedXADataSource;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class PoolDataSourceCreator implements DataSourceCreator {
    protected final Map<Object, ObjectRecipe> recipes = new HashMap<>();

    protected void cleanProperty(final Object ds, final String name) {
        final Map<String, Object> unsetProperties = recipes.get(ds).getUnsetProperties();
        unsetProperties.entrySet().removeIf(entry -> entry.getKey().equalsIgnoreCase(name));
    }

    @Override
    public DataSource managed(final String name, final CommonDataSource ds) {
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds, transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        }
        return new ManagedDataSource(DataSource.class.cast(ds), transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
    }

    @Override
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        final TransactionManager transactionManager = new TransactionManagerWrapper(OpenEJB.getTransactionManager(), name, xaResourceWrapper);
        final CommonDataSource ds = pool(name, driver, properties);
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds, transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        }
        return new ManagedDataSource(DataSource.class.cast(ds), transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds, final Properties properties) {
        return managed(name, pool(name, ds, properties));
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        return managed(name, pool(name, driver, properties));
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        if (object instanceof ManagedDataSource) {
            doDestroy(((ManagedDataSource) object).getDelegate());
        } else {
            doDestroy((DataSource) object);
        }
    }

    protected abstract void doDestroy(CommonDataSource dataSource) throws Throwable;

    protected <T> T build(final Class<T> clazz, final Properties properties) {
        final ObjectRecipe serviceRecipe = new ObjectRecipe(clazz);
        recipeOptions(serviceRecipe);
        serviceRecipe.setAllProperties(properties);
        final T value = (T) serviceRecipe.create();
        if (trackRecipeFor(value)) { // avoid to keep config objects
            recipes.put(value, serviceRecipe);
        }
        return value;
    }

    protected boolean trackRecipeFor(final Object value) {
        return value instanceof DataSource;
    }

    protected <T> T build(final Class<T> clazz, final Object instance, final Properties properties) {
        final ObjectRecipe recipe = PassthroughFactory.recipe(instance);
        recipeOptions(recipe);
        recipe.setAllProperties(properties);
        final T value = (T) recipe.create();
        recipes.put(value, recipe);
        return value;
    }

    private void recipeOptions(final ObjectRecipe recipe) { // important to not set "properties" attribute because pools often use it for sthg else
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
    }

    @Override
    public ObjectRecipe clearRecipe(final Object object) {
        if (object instanceof ManagedDataSource) {
            return recipes.remove(((ManagedDataSource) object).getDelegate());
        } else {
            return recipes.remove(object);
        }
    }
}
