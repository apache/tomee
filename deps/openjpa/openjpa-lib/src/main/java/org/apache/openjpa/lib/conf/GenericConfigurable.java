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

import org.apache.openjpa.lib.util.Options;

/**
 * Implementations of this interface may perform additional
 * generic configuration with any key-value pairs that cannot be set
 * into the object via the normal 
 * {@link org.apache.openjpa.lib.util.Options#setInto} means.
 *
 * @author Patrick Linskey
 */
public interface GenericConfigurable {

    /**
     * Perform any generic configuration based on the data in
     * <code>opts</code>. This method should remove any values in
     * <code>opts</code> that have been successfully processed; if any
     * values remain in <code>opts</code> after this method is executed,
     * an exception will be thrown identifying those key-value pairs as invalid.
     */
    public void setInto(Options opts);
}
