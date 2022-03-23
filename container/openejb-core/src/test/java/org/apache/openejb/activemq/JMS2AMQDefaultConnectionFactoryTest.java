/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.activemq;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SimpleLog
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@ContainerProperties(@ContainerProperties.Property(name = "openejb.environment.default", value = "true")) // off in embedded mode OOTB
public class JMS2AMQDefaultConnectionFactoryTest {
    @Inject
    private JMSContext defaultContext;

    @Inject
    private JustToGetAJndiContext justToGetAJndiContext;

    @Test
    public void checkCF() throws Exception {
        assertEquals("test", defaultContext.createTextMessage("test").getText());
        justToGetAJndiContext.checkJndi();
    }

    @Singleton
    public static class JustToGetAJndiContext {
        public void checkJndi() {
            try {
                assertTrue(ConnectionFactory.class.isInstance(new InitialContext().lookup("java:comp/DefaultJMSConnectionFactory")));
            } catch (final NamingException e) {
                fail();
            }
        }
    }
}
