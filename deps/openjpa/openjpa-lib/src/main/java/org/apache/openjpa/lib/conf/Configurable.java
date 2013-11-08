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
package org.apache.openjpa.lib.conf;

/**
 * This interface provides a mechanism for notifying interested
 * objects when configuration events occur. It provides an object
 * with the opportunity to set itself up for configuration and to
 * perform any necessary post-configuration.
 *
 * @author Patrick Linskey
 */
public interface Configurable {

    /**
     * Invoked prior to setting bean properties.
     */
    public void setConfiguration(Configuration conf);

    /**
     * Invoked before bean property configuration is begun on this object.
     */
    public void startConfiguration();

    /**
     * Invoked upon completion of bean property configuration for this object.
     */
    public void endConfiguration();
}
