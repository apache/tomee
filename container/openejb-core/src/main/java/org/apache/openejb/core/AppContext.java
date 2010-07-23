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
package org.apache.openejb.core;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;

import javax.naming.Context;

/**
 * @version $Rev$ $Date$
*/
public class AppContext extends DeploymentContext {
    private final SystemInstance systemInstance;
    private final ClassLoader classLoader;
    private final Context globalJndiContext;
    private final Context appJndiContext;

    public AppContext(String id, SystemInstance systemInstance, ClassLoader classLoader, Context globalJndiContext, Context appJndiContext) {
        super(id, systemInstance.getOptions());
        this.classLoader = classLoader;
        this.systemInstance = systemInstance;
        this.globalJndiContext = globalJndiContext;
        this.appJndiContext = appJndiContext;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public SystemInstance getSystemInstance() {
        return systemInstance;
    }

    public Context getAppJndiContext() {
        return appJndiContext;
    }

    public Context getGlobalJndiContext() {
        return globalJndiContext;
    }
}
