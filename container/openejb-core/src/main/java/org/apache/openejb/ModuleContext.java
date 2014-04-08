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
package org.apache.openejb;

import java.net.URI;

import javax.naming.Context;

/**
 * @version $Rev$ $Date$
*/
public class ModuleContext extends DeploymentContext {
    private final AppContext appContext;
    private final Context moduleJndiContext;
    private final String uniqueId;
    private final URI moduleURI;
    private final ClassLoader loader;

    public ModuleContext(String id, URI moduleURI, String uniqueId, AppContext appContext, Context moduleJndiContext, ClassLoader classLoader) {
        super(id, appContext.getOptions());
        this.moduleURI = moduleURI;
        this.appContext = appContext;
        this.moduleJndiContext = moduleJndiContext;
        this.uniqueId = uniqueId;
        if (classLoader != null) {
            this.loader = classLoader;
        } else { // in tests for instance but shouldn't be the case in main part of the program
            this.loader = Thread.currentThread().getContextClassLoader();
        }
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public Context getModuleJndiContext() {
        return moduleJndiContext;
    }

    public String getUniqueId() {
        return uniqueId;
    }
    
    public URI getModuleURI() {
        return moduleURI;
    }    
}
