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
package org.apache.openjpa.slice;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.slice.jdbc.TargetFetchConfiguration;

/**
 * Extension to Broker to allow access to {@link DistributedStoreManager virtual data store}.
 * The broker manages a single persistence context like a normal {@link Broker}, but the
 * context holds data from multiple databases. 
 * 
 * @author Pinaki Poddar
 *
 */
public interface DistributedBroker extends Broker {
    /**
     * Gets the distributed store manager used by this receiver.
     */
    DistributedStoreManager getDistributedStoreManager();
   
    /**
     * Gets the covariant fetch configuration that is aware of targets.
     */
    TargetFetchConfiguration getFetchConfiguration();
}
