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

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.JMSDestination;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.KeyedCollection;
import org.apache.openejb.jee.Property;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.List;
import java.util.Properties;

public class ConvertJMSDestinationDefinitions extends BaseConvertDefinitions {
    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        final List<JndiConsumer> jndiConsumers = collectConsumers(appModule);
        final KeyedCollection<String, JMSDestination> destinations = new KeyedCollection<>();
        for (final JndiConsumer consumer : jndiConsumers) {
            if (consumer != null) {
                destinations.addAll(consumer.getJMSDestination());
            }
        }
        for (final JMSDestination destination : destinations) {
            appModule.getResources().add(toResource(destination));
        }
        return appModule;
    }


    private Resource toResource(final JMSDestination factory) {
        final String name = cleanUpName(factory.getName());

        final Resource factoryResource = new Resource(name, jakarta.jms.ConnectionFactory.class.getName());

        factoryResource.setJndi(factory.getName().replaceFirst("java:", ""));
        factoryResource.setType(
            factory.getInterfaceName() != null && !factory.getInterfaceName().isEmpty() ?
                factory.getInterfaceName() : "jakarta.jms.Queue");
        if (factory.getClassName() != null && !factory.getClassName().isEmpty()) {
            factoryResource.setClassName(factory.getClassName());
        }

        final Properties p = factoryResource.getProperties();
        put(p, AutoConfig.ORIGIN_FLAG, AutoConfig.ORIGIN_ANNOTATION);
        put(p, "JndiName", factoryResource.getJndi());
        put(p, "Destination", factory.getDestinationName());
        if (factory.getResourceAdapter() != null && !factory.getResourceAdapter().isEmpty()) {
            put(p, "ResourceAdapter", factory.getResourceAdapter());
        }

        setProperties(factory, p);
        return factoryResource;
    }

    private void setProperties(final JMSDestination d, final Properties p) {
        for (final Property property : d.getProperty()) {
            final String key = property.getName();
            final String value = property.getValue();
            put(p, key, value);
        }
    }

    private static void put(final Properties properties, final String key, final Object value) {
        if (key != null && value != null) {
            properties.put(key, PropertyPlaceHolderHelper.value(String.valueOf(value)));
        }
    }
}
