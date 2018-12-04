/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.connector.adapter;

import org.superbiz.connector.api.SampleConnection;

import java.util.logging.Logger;

public class SampleConnectionImpl implements SampleConnection {
    private static Logger log = Logger.getLogger(SampleConnectionImpl.class.getName());

    private SampleManagedConnection mc;

    private SampleManagedConnectionFactory mcf;

    public SampleConnectionImpl(SampleManagedConnection mc, SampleManagedConnectionFactory mcf) {
        this.mc = mc;
        this.mcf = mcf;
    }

    public void sendMessage(final String message) {
        mc.sendMessage(message);
    }

    public void close() {
        mc.closeHandle(this);
    }
}
