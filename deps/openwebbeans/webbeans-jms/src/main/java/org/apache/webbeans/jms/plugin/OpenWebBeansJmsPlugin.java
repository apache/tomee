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
package org.apache.webbeans.jms.plugin;

import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.jms.JMSModel;
import org.apache.webbeans.jms.component.JmsComponentFactory;
import org.apache.webbeans.jms.component.JmsBean;
import org.apache.webbeans.jms.util.JmsProxyHandler;
import org.apache.webbeans.jms.util.JmsUtil;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;

/**
 * JMS Plugin for JMS related components.
 * 
 * @version $Rev$ $Date$
 */
public class OpenWebBeansJmsPlugin extends AbstractOwbPlugin implements org.apache.webbeans.plugins.OpenWebBeansJmsPlugin
{

    public OpenWebBeansJmsPlugin()
    {
        super();
    }

    public Bean<?> getJmsBean(JMSModel model)
    {
        JmsBean<?> bean = JmsComponentFactory.getJmsComponentFactory().getJmsComponent(model);

        return bean;

    }

    @Override
    public void shutDown() throws WebBeansConfigurationException
    {
        JmsProxyHandler.clearConnections();
    }

    public Object getJmsBeanProxy(Bean<?> bean, Class<?> iface)
    {
        Object proxy = JmsUtil.createNewJmsProxy((JmsBean<?>) bean, iface);

        return proxy;
    }

}
