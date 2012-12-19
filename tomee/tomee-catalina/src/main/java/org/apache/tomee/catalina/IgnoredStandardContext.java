/**
 *
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
package org.apache.tomee.catalina;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;

public class IgnoredStandardContext extends StandardContext {
    public IgnoredStandardContext() {
        // Tomcat has a stupid rule where a life cycle listener must set
        // configured true, or it will treat it as a failed deployment
        addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                Context context = (Context) event.getLifecycle();
                if (event.getType().equals(Lifecycle.START_EVENT)
                        || event.getType().equals(Lifecycle.BEFORE_START_EVENT)
                        || event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
                    context.setConfigured(true);
                }
            }
        });
    }
}
