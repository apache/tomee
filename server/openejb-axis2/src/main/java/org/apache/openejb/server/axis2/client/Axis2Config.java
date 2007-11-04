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
package org.apache.openejb.server.axis2.client;

import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;

public class Axis2Config {
    public synchronized static void initialize() {
        ClientConfigurationFactory factory = (ClientConfigurationFactory) MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        if (factory instanceof Axis2ClientConfigurationFactory) {
            return;
        }

        factory = new Axis2ClientConfigurationFactory(false);
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, factory);

        // ensure that the factory was installed at the right time
        if (factory != DescriptionFactoryImpl.getClientConfigurationFactory()) {
            throw new RuntimeException("Client configuration factory was registered too late");
        }
    }
}
