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
package org.apache.openejb.server.cxf.pojo;

import org.apache.cxf.Bus;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.CxfWsContainer;

import javax.naming.Context;

public class PojoWsContainer extends CxfWsContainer {
    private final Context context;
    private final Class target;

    public PojoWsContainer(Bus bus, PortData port, Context context, Class target) {
        super(bus, port);
        if (target == null) throw new NullPointerException("target is null");
        this.context = context;
        this.target = target;
    }

    protected PojoEndpoint createEndpoint() {
        PojoEndpoint ep = new PojoEndpoint(bus, port, context, target);
        return ep;
    }

    public void start() {
        super.start();
        this.destination.setPassSecurityContext(true);
    }
}
