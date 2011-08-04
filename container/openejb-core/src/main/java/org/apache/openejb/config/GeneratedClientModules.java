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
import org.apache.openejb.jee.ApplicationClient;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encompasses a little technique that saves lots of architecture rework.
 *
 * Essentially we're allowing an EjbModule to be both an EjbModule and a ClientModule.
 * Trick is we don't really know if it has any @LocaClient or @RemoteClient classes
 * until we've scanned it.  Since it's already an EjbModule and we do plan to scan
 * it, we just automatically generate a ClientModule for all EjbModules and ensure that
 * the ClientModule will be able to reuse the ClassFinder instance, which is a pretty
 * heavy object created by reading all of the class files in a jar via ASM.  We really
 * don't want to do that twice if we don't have to.  We link them by giving them the
 * same AtomicReference<ClassFinder> object.  When one of them sets it, they both see it
 * and the need to create a second one is avoided.
 *
 * If the automatically generated ClientModule doesn't turn out to really be a client after
 * any descriptors have been read and the jar scanned, then we just remove it so it doesn't
 * factor into the remainder of the deploy chain.
 */
public class GeneratedClientModules {

    /**
     * Add the auto-generated and linked ClientModule from each EjbModule
     */
    public static class Add implements DynamicDeployer {
        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                if (ejbModule.getClientModule() != null) {
                    appModule.getClientModules().add(ejbModule.getClientModule());
                    ejbModule.setClientModule(null);
                }
            }
            return appModule;
        }
    }

    /**
     * Clean up any that didn't turn out to have any actual ejb clients
     */
    public static class Prune implements DynamicDeployer {
        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            List<ClientModule> clientModules = new ArrayList<ClientModule>(appModule.getClientModules());

            for (ClientModule clientModule : clientModules) {
                // we automatically add a ClientModule to every EjbModule
                // if there didn't turn out to be any clients in the module
                // just ingore it and remove it from the clientModule list
                boolean haveMainClassAndDescriptor = clientModule.getMainClass() != null && clientModule.getApplicationClient() != null;
                boolean haveAnnotatedClients = clientModule.getLocalClients().size() > 0 || clientModule.getRemoteClients().size() > 0;

                if (clientModule.isEjbModuleGenerated() && !haveMainClassAndDescriptor && !haveAnnotatedClients) {
                    appModule.getClientModules().remove(clientModule);
                } else if (clientModule.getApplicationClient() == null) {
                    // If we're keeping it, make sure it has an ApplicationClient object.
                    // Several places in the deploy chain check the contents of the JndiConsumer,
                    // which is the ApplicationClient JAXB object for this module type.
                    clientModule.setApplicationClient(new ApplicationClient());
                }
            }

            return appModule;
        }
    }
}
