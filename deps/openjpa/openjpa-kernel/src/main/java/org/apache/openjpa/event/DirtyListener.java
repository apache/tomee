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
 * Listener for when a persistent instance becomes dirty.
 *
 * @author Steve Kim
 * @author Abe White
 */
public interface DirtyListener {

    /**
     * Invoked before the first change is applied.
     */
    public void beforeDirty(LifecycleEvent event);

    /**
     * Invoked after the first change is applied.
     */
    public void afterDirty(LifecycleEvent event);

    /**
     * Invoked before the first change is applied to a flushed instance.
     */
    public void beforeDirtyFlushed(LifecycleEvent event);

    /**
     * Invoked after the first change is applied to a flushed instance.
     */
    public void afterDirtyFlushed(LifecycleEvent event);
}
