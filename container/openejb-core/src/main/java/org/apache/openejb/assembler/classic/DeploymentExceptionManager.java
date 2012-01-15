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

package org.apache.openejb.assembler.classic;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tomcat swallows some exception when trying to deploy a war.
 * To be able to get it back (from our Deployers for instance)
 * we need a way to store it.
 *
 */
public class DeploymentExceptionManager {
    private static final int MAX_SIZE = Integer.getInteger("tomee.deployement-exception-max-size", 10);
    private final Map<AppInfo, Exception> deploymentException = new LinkedHashMap<AppInfo, Exception>() {
        @Override // just to avoid potential memory leak
        protected boolean removeEldestEntry(Map.Entry<AppInfo, Exception> eldest) {
            return size() > MAX_SIZE;
        }
    };
    private Exception lastException = null;

    public synchronized boolean hasDeploymentFailed() {
        return lastException != null;
    }

    public synchronized Exception getDeploymentException(final AppInfo appInfo) {
        return deploymentException.get(appInfo);
    }

    public synchronized Exception saveDeploymentException(final AppInfo appInfo, final Exception exception) {
        lastException = exception;
        return deploymentException.put(appInfo, exception);
    }

    public synchronized void clearLastException(final AppInfo info) {
        if (info != null && deploymentException.get(info) == lastException) {
            deploymentException.remove(info);
        }
        lastException = null;
    }

    public Exception getLastException() {
        return lastException;
    }

    public void pushDelpoymentException(final Exception exception) {
        lastException = exception;
    }
}
