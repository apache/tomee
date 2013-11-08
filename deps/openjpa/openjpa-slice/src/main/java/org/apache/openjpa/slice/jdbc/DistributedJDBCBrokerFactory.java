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
package org.apache.openjpa.slice.jdbc;

import java.security.AccessController;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCBrokerFactory;
import org.apache.openjpa.kernel.Bootstrap;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.slice.DistributedBroker;
import org.apache.openjpa.slice.DistributedBrokerFactory;
import org.apache.openjpa.slice.DistributedBrokerImpl;
import org.apache.openjpa.slice.Slice;

/**
 * A factory for distributed JDBC datastores.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public class DistributedJDBCBrokerFactory extends JDBCBrokerFactory 
    implements DistributedBrokerFactory {
	private static final Localizer _loc = Localizer.forPackage(DistributedJDBCBrokerFactory.class);
	
	/**
     * Factory method for constructing a factory from properties. Invoked from
	 * {@link Bootstrap#newBrokerFactory}.
	 */
	public static DistributedJDBCBrokerFactory newInstance(ConfigurationProvider cp) {
		DistributedJDBCConfigurationImpl conf =	new DistributedJDBCConfigurationImpl();
		cp.setInto(conf);
		return new DistributedJDBCBrokerFactory(conf);
	}

	/**
     * Factory method for obtaining a possibly-pooled factory from properties.
	 * Invoked from {@link Bootstrap#getBrokerFactory}.
	 */
	public static JDBCBrokerFactory getInstance(ConfigurationProvider cp) {
	    Map<String,Object> properties = cp.getProperties();
	    Object key = toPoolKey(properties);
		DistributedJDBCBrokerFactory factory = (DistributedJDBCBrokerFactory) getPooledFactoryForKey(key);
		if (factory != null)
			return factory;

		factory = newInstance(cp);
		pool(key, factory);
		return factory;
	}

	/**
	 * Factory method for constructing a factory from a configuration.
	 */
	public static synchronized JDBCBrokerFactory getInstance(DistributedJDBCConfiguration conf) {
	    Map<String,Object> properties = conf.toProperties(false);
	    Object key = toPoolKey(properties);
		DistributedJDBCBrokerFactory factory = (DistributedJDBCBrokerFactory) getPooledFactoryForKey(key);
		if (factory != null)
			return factory;

		factory = new DistributedJDBCBrokerFactory(conf);
		pool(key, factory);
		return factory;
	}

	public DistributedJDBCBrokerFactory(DistributedJDBCConfiguration conf) {
		super(conf);
	}
	
	@Override
	public DistributedJDBCConfiguration getConfiguration() {
	    return (DistributedJDBCConfiguration)super.getConfiguration();
	}
	
	public Slice addSlice(String name, Map properties) {
	    Slice slice = ((DistributedJDBCConfigurationImpl)getConfiguration()).addSlice(name, properties);
        ClassLoader loader = AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
        synchronizeMappings(loader, (JDBCConfiguration)slice.getConfiguration());
        Collection<Broker> brokers = getOpenBrokers();
        for (Broker broker : brokers) {
            ((DistributedBroker)broker).getDistributedStoreManager().addSlice(slice);
        }
	    return slice;
	}

	@Override
	protected DistributedJDBCStoreManager newStoreManager() {
		return new DistributedJDBCStoreManager(getConfiguration());
	}
    
    @Override
    public DistributedBroker newBroker() {
        return new DistributedBrokerImpl();
    }
    
    protected void synchronizeMappings(ClassLoader loader) {
        List<Slice> slices = getConfiguration().getSlices(Slice.Status.ACTIVE);
        for (Slice slice : slices) {
            synchronizeMappings(loader, (JDBCConfiguration) slice.getConfiguration());
        }
    }

    @Override
    protected Object getFactoryInitializationBanner() {
        return _loc.get("factory-init", OpenJPAVersion.VERSION_NUMBER);
    }
}
