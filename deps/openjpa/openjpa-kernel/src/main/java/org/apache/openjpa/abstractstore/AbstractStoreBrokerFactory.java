/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.abstractstore;

import java.security.AccessController;
import java.util.Map;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.kernel.Bootstrap;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * {@link BrokerFactory} implementation for use with the
 * {@link AbstractStoreManager}. This provides integration into the
 * {@link Bootstrap#getBrokerFactory()} bootstrapping mechanism, to facilitate
 * the process of creating a subclass of {@link AbstractStoreManager}. New
 * store manager implementations need not extend this class. Instead, set the
 * <code>openjpa.BrokerFactory</code> configuration property to
 * <code>abstractstore</code>,
 * and set the <code>openjpa.abstractstore.AbstractStoreManager</code>
 * configuration property to the full class name of your implementation.
 *  Additionally, you can optionally create your own
 * <code>BrokerFactory</code> implementation. However, we recommend that you
 * use the <code>AbstractStoreBrokerFactory</code>, as it deals with pooling
 * and bootstrapping from a {@link Map} object (the strategy used by
 * {@link Bootstrap} to create a factory in a vendor-neutral manner).
 */
public class AbstractStoreBrokerFactory
    extends AbstractBrokerFactory {

    /**
     * The property name under which to name the concrete store manager
     * class for this runtime.
     */
    private static final String PROP_ABSTRACT_STORE =
        "abstractstore.AbstractStoreManager";

    private static final Localizer s_loc = Localizer.forPackage
        (AbstractStoreBrokerFactory.class);

    private String _storeCls = null;
    private String _storeProps = null;
    private String _platform = null;

    /**
     * Factory method for obtaining a possibly-pooled {@link BrokerFactory}
     * from properties. Invoked from {@link Bootstrap#getBrokerFactory()}.
     */
    public static AbstractStoreBrokerFactory getInstance(
        ConfigurationProvider cp) {
        Object key = toPoolKey(cp.getProperties());
        AbstractStoreBrokerFactory factory = (AbstractStoreBrokerFactory)
            getPooledFactoryForKey(key);
        if (factory != null)
            return factory;

        factory = newInstance(cp);
        factory.pool(key, factory);
        return factory;
    }

    /**
     * Factory method for constructing a {@link BrokerFactory}
     * from properties. Invoked from {@link Bootstrap#newBrokerFactory()}.
     */
    public static AbstractStoreBrokerFactory newInstance
        (ConfigurationProvider cp) {
        // use a tmp store manager to get metadata about the capabilities of
        // this runtime
        Map map = cp.getProperties();
        String storePlugin = (String) map.get(ProductDerivations
            .getConfigurationKey(PROP_ABSTRACT_STORE, map));
        String storeCls = Configurations.getClassName(storePlugin);
        String storeProps = Configurations.getProperties(storePlugin);
        AbstractStoreManager store = createStoreManager(storeCls,
            storeProps);

        // populate configuration
        OpenJPAConfiguration conf = store.newConfiguration();
        cp.setInto(conf);
        conf.supportedOptions().removeAll(store.getUnsupportedOptions());

        // create and pool a new factory
        return new AbstractStoreBrokerFactory(conf, storeCls, storeProps,
            store.getPlatform());
    }

    /**
     * Construct the factory with the given settings.
     */
    protected AbstractStoreBrokerFactory(OpenJPAConfiguration conf,
        String storeCls, String storeProps, String platform) {
        super(conf);
        _storeCls = storeCls;
        _storeProps = storeProps;
        _platform = platform;
    }

    public Map<String,Object> getProperties() {
        Map<String,Object> props = super.getProperties();
        props.put("Platform", _platform);
        return props;
    }

    protected StoreManager newStoreManager() {
        return createStoreManager(_storeCls, _storeProps);
    }

    private static AbstractStoreManager createStoreManager(String cls,
        String props) {
        AbstractStoreManager store =
            (AbstractStoreManager) Configurations.newInstance(cls,
                AccessController.doPrivileged(J2DoPrivHelper
                    .getClassLoaderAction(AbstractStoreManager.class))); 
        Configurations.configureInstance(store, null, props,
            PROP_ABSTRACT_STORE);
        if (store == null)
            throw new UserException(s_loc.get("no-store-manager",
                PROP_ABSTRACT_STORE)).setFatal(true);

        return store;
	}
}
