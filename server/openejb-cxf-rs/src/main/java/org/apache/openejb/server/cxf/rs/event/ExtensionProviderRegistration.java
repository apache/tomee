/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs.event;

import org.apache.openejb.AppContext;
import org.apache.openejb.observer.Event;

import java.util.List;

// using list reference to be able to get updates in CxfrsHttpListener automatically
// this event can allow to add/remove/resort providers
@Event
public class ExtensionProviderRegistration {
    private final List<Object> providers;
    private final AppContext appContext;

    public ExtensionProviderRegistration(final AppContext ctx, final List<Object> providers) {
        this.appContext = ctx;
        this.providers = providers;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public List<Object> getProviders() {
        return providers;
    }

    @Override
    public String toString() {
        return "ExtensionProviderRegistration{}";
    }
}
