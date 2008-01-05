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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ClearEmptyMappedName implements DynamicDeployer {

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            for (JndiConsumer consumer : ejbModule.getEjbJar().getEnterpriseBeans()) {
                clearEmptyMappedName(consumer);
            }
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            clearEmptyMappedName(clientModule.getApplicationClient());
        }
        for (WebModule webModule : appModule.getWebModules()) {
            clearEmptyMappedName(webModule.getWebApp());
        }
        return appModule;
    }

    private void clearEmptyMappedName(JndiConsumer consumer) {
        List<JndiReference> refs = new ArrayList<JndiReference>();
        refs.addAll(consumer.getEjbLocalRef());
        refs.addAll(consumer.getEjbRef());
        refs.addAll(consumer.getEnvEntry());
        refs.addAll(consumer.getMessageDestinationRef());
        refs.addAll(consumer.getPersistenceContextRef());
        refs.addAll(consumer.getPersistenceUnitRef());
        refs.addAll(consumer.getResourceEnvRef());
        refs.addAll(consumer.getResourceRef());
        refs.addAll(consumer.getServiceRef());

        for (JndiReference ref : refs) {
            if (ref.getMappedName() != null && ref.getMappedName().length() == 0) ref.setMappedName(null);
        }
    }
}
