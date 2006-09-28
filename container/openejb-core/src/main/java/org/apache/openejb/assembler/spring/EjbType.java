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
package org.apache.openejb.assembler.spring;

import org.apache.openejb.DeploymentInfo;

public class EjbType {
    public static final EjbType STATEFUL = new EjbType(DeploymentInfo.STATEFUL, "Stateful SessionBean");
    public static final EjbType STATELESS = new EjbType(DeploymentInfo.STATELESS, "Stateless SessionBean");
    public static final EjbType CMP_ENTITY = new EjbType(DeploymentInfo.CMP_ENTITY, "CMP EntityBean");
    public static final EjbType BMP_ENTITY = new EjbType(DeploymentInfo.BMP_ENTITY, "BMP EntityBean");

    private final boolean isSession;
    private final boolean isEntity;
    private final byte type;
    private final String typeName;

    private EjbType(byte type, String typeName) {
        this.type = type;
        this.typeName = typeName;
        isSession = org.apache.openejb.core.CoreDeploymentInfo.STATEFUL == type || org.apache.openejb.core.CoreDeploymentInfo.STATELESS == type;
        isEntity = !isSession;
    }

    public boolean isSession() {
        return isSession;
    }

    public boolean isEntity() {
        return isEntity;
    }

    public byte getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }
}
