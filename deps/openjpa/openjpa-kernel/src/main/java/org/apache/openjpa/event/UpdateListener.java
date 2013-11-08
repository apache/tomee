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
package org.apache.openjpa.event;

/**
 * @since 1.1.0
 */
public interface UpdateListener {

    /**
     * Receives notifications before an update is performed. Differs from
     * {@link StoreListener#beforeStore} in that the latter is called for
     * updated and new records, whereas this is only invoked for updated
     * records.
     */
    public void beforeUpdate(LifecycleEvent event);

    /**
     * Receives notifications before an update is performed. Differs from
     * {@link StoreListener#afterStore} in that the latter is called for
     * updated and new records, whereas this is only invoked for updated
     * records, and that this is called after the record is actually flushed
     * to the store.
     */
    public void afterUpdatePerformed(LifecycleEvent event);
}
