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
package org.apache.openjpa.persistence.callbacks;

import javax.persistence.PostLoad;

/**
 * JPA Listener which maintains changelog information of the {@link PostLoadListenerEntity}.
 * The &#064;PostLoad gets called once the entity is being loaded from the database.
 * This happens either if the entity get's loaded freshly into the EntityManager, or 
 * while performing a call to EntityManager#merge(entity)
 */
public class PostLoadListenerImpl {

    static String postLoadValue;
    
    @PostLoad
    public void postLoad(Object o) {
        PostLoadListenerEntity ple = (PostLoadListenerEntity) o;
        
        postLoadValue = ple.getValue();
    }
}
