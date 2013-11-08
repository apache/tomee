/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.kernel;

/**
 * Subtype of {@link BrokerImpl} that automatically closes itself during
 * finalization. This implementation guards against potential resource leaks,
 * but also incurs a scalability bottleneck as instances are enlisted in the
 * finalization queue and subsequently cleaned up.
 *
 * @since 0.9.7
 */
public class FinalizingBrokerImpl
    extends BrokerImpl {

    /**
     * Close on finalize.
     */
    protected void finalize()
        throws Throwable {
        super.finalize();
        if (!isClosed())
            free();
    }
}
