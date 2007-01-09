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
package org.apache.openejb.alt.config;

import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.ResourceRef;

public class SessionBean implements Bean {

    org.apache.openejb.jee.SessionBean bean;
    String type;

    SessionBean(org.apache.openejb.jee.SessionBean bean) {
        this.bean = bean;
        if (bean.getSessionType() == SessionType.STATEFUL) {
            type = STATEFUL;
        } else {
            type = STATELESS;
        }
    }

    public String getType() {
        return type;
    }

    public Object getBean() {
        return bean;
    }

    public String getEjbName() {
        return bean.getEjbName();
    }

    public String getEjbClass() {
        return bean.getEjbClass();
    }

    public String getHome() {
        return bean.getHome();
    }

    public String getRemote() {
        return bean.getRemote();
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef().toArray(new ResourceRef[]{});
    }
}

