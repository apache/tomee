/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.jee.ResourceRef;

public interface Bean {

    public static final String BMP_ENTITY = "BMP_ENTITY";
    public static final String CMP_ENTITY = "CMP_ENTITY";
    public static final String CMP2_ENTITY = "CMP2_ENTITY";
    public static final String STATEFUL = "STATEFUL";
    public static final String STATELESS = "STATELESS";
    public static final String MESSAGE = "MESSAGE";

    public String getType();

    public Object getBean();

    public String getEjbName();

    public String getEjbClass();

    public String getHome();

    public String getRemote();

    public String getLocalHome();

    public String getLocal();

    public ResourceRef[] getResourceRef();
}

