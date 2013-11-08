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
package org.apache.openjpa.lib.instrumentation;

/**
 * The instrumentation level can be used to indicate if and when an instrument will be
 * automatically started and stopped.
 *
 */
public enum InstrumentationLevel {
    /**
     *  Start immediately (no special requirements on the broker or factory) and
     *  stop when the configuration is closed. 
     */
    IMMEDIATE, 
    /**
     * Start following factory initialization and stop when the factory is closed.
     */
    FACTORY,
    /**
     * Start following broker/em initialization and stop when the broker/em is closed.
     */
    BROKER,
    /**
     * Manual start and stop.
     */
    MANUAL
}
