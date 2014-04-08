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

package org.apache.openejb.core.builder;

import org.apache.openejb.assembler.classic.MdbConfigTest.FakeMdb;
import org.apache.openejb.assembler.classic.MdbConfigTest.FakeMessageListener;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.MessageDrivenBean;

public class MdbBuilder {

    ActivationConfig activationConfig = new ActivationConfig();
    MessageDrivenBean messageDrivenBean;

    public MdbBuilder anMdb() {
        messageDrivenBean = createJaxbMdb("FakeMdb", FakeMdb.class.getName(), FakeMessageListener.class.getName());
        return this;
    }

    public MdbBuilder withActivationProperty(String key, String value) {

        activationConfig.getActivationConfigProperty().add(new ActivationConfigProperty(key, value) {
        });
        return this;
    }

    public MessageDrivenBean build() {
        messageDrivenBean.setActivationConfig(activationConfig);
        return messageDrivenBean;
    }

    private MessageDrivenBean createJaxbMdb(String ejbName, String mdbClass, String messageListenerInterface) {
        MessageDrivenBean bean = new MessageDrivenBean(ejbName);
        bean.setEjbClass(mdbClass);
        bean.setMessagingType(messageListenerInterface);

        return bean;
    }

}
