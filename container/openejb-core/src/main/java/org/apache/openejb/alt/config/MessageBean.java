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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.config;

import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.ResourceRef;

public class MessageBean implements Bean {

    org.apache.openejb.jee.MessageDrivenBean bean;
    String type;

    MessageBean(MessageDrivenBean bean2) {
        this.bean = bean2;
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
        return null;
    }

    public String getRemote() {
        return null;
    }

    public String getLocal() {
        return null;
    }

    public String getLocalHome() {
        return null;
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef().toArray(new ResourceRef[]{});
    }
}
