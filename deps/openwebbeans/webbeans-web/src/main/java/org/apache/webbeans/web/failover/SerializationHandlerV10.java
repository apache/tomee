/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.web.failover;

import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.enterprise.inject.spi.Bean;

/**
 * Application could implement this interface and register with failover service to handle 
 * serialization/deserialization of resource beans. Failover serivce will invoke this
 * handleResource Method then.
 * 
 */
public interface SerializationHandlerV10 
{
    /**
     * failover case.
     */
    public final static int TYPE_FAILOVER = 0;

    /**
     * passivation case.
     */
    public final static int TYPE_PASSIVATION = 1;
        
    /**
     * Application provided custom handler for serialize 
     * and deserialize resource beans.
     *  
     * @param bean                The resource bean.
     * @param resourceObject    The resource bean instance
     * @param in                The input object stream
     * @param out                The output object stream
     * @param type                TYPE_FAILOVER or TYPE_PASSIVATION
     * 
     * @return NOT_HANDLED if not handled by handler.
     */
    public Object handleResource(
            Bean<?> bean,
            Object resourceObject,
            ObjectInput in,
            ObjectOutput out,
            int type
            );

}
