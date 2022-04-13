/*
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
package org.apache.openejb.core.mdb;

import org.apache.activemq.ra.ActiveMQActivationSpec;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ActivationConfigTest {

    @Test
    public void testShouldResolvePlaceHolder() throws Exception {
        final ResourceAdapter ra = new ActiveMQResourceAdapter();
        final MdbContainer container = new MdbContainer("TestMdbContainer", null, ra, MessageListener.class, ActiveMQActivationSpec.class, 10, false);


        final Map<String, String> properties = new HashMap<>();
        properties.put("clientId", "{appId}#{ejbJarId}#{ejbName}#{hostName}#{uniqueId}");
        properties.put("subscriptionName", "subscription-{appId}#{ejbJarId}#{ejbName}#{hostName}#{uniqueId}");
        properties.put("destination", "MyTopic");
        properties.put("destinationType", "javax.jms.Topic");

        final BeanContext beanContext = getMockBeanContext(properties);

        final ActivationSpec activationSpec = container.createActivationSpec(beanContext);
        Assert.assertTrue(activationSpec instanceof ActiveMQActivationSpec);

        ActiveMQActivationSpec spec = (ActiveMQActivationSpec) activationSpec;

        final String clientId = spec.getClientId();
        final String[] parts = clientId.split("#");

        final String hostname = (InetAddress.getLocalHost()).getHostName();

        Assert.assertEquals("appId", parts[0]);
        Assert.assertEquals("moduleId", parts[1]);
        Assert.assertEquals("MyEjb", parts[2]);
        Assert.assertEquals(hostname, parts[3]);

        final Pattern pattern = Pattern.compile("ID:.*?-\\d+-\\d+-\\d+:\\d");
        Assert.assertTrue(pattern.matcher(parts[4]).matches());
    }

    private BeanContext getMockBeanContext(final Map<String, String> properties) throws Exception {
        final IvmContext context = new IvmContext();
        context.bind("comp/Validator", new NoOpValidator());

        final AppContext mockAppContext = new AppContext("appId", SystemInstance.get(),  this.getClass().getClassLoader(), context, context, false);
        final ModuleContext mockModuleContext =  new ModuleContext("moduleId", new URI(""), "uniqueId", mockAppContext, context, this.getClass().getClassLoader());
        final BeanContext mockBeanContext = new BeanContext("test", context, mockModuleContext, MyListener.class, MessageListener.class, properties);
        mockBeanContext.setEjbName("MyEjb");

        return mockBeanContext;
    }

    public static class MyListener implements MessageListener {

        @Override
        public void onMessage(Message message) {

        }
    }

    public static class NoOpValidator implements Validator {
        @Override
        public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
            return Collections.emptySet();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
            return Collections.emptySet();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
            return Collections.emptySet();
        }

        @Override
        public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return null;
        }

        @Override
        public ExecutableValidator forExecutables() {
            return null;
        }
    }
}
