/**
 *
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
package org.apache.openejb.server.cxf;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@EnableServices("jaxws")
@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true)
public class DontSerializeBeforeCommitTest {
    @RandomPort("http")
    private int port;

    @EJB
    private TheTxImpl bean;

    @Test
    public void test() throws MalformedURLException {
        bean.setCounter(0);
        final TheTx client = Service.create(new URL("http://localhost:" + port + "/openejb/TheTxImpl?wsdl"),
                new QName("http://cxf.server.openejb.apache.org/", "TheTxImplService"))
                .getPort(TheTx.class);
        try {
            client.compute(false);
            fail();
        } catch (final SOAPFaultException fault) {
            assertTrue(fault.getMessage().contains("Transaction was rolled back"));
        }
        assertEquals(2, client.compute(true).getValue());
    }

    @WebService
    public interface TheTx {
        TheResult compute(boolean passing);
    }

    @WebService
    @Singleton
    @LocalBean
    @HandlerChain(file = "passthrough-handler.xml")
    public static class TheTxImpl implements TheTx {
        @Resource
        private TransactionSynchronizationRegistry registry;

        private int counter = 0;

        public int getCounter() {
            return counter;
        }

        public void setCounter(final int counter) {
            this.counter = counter;
        }

        @Override
        public TheResult compute(final boolean passing) {
            final TheResult theResult = new TheResult();
            counter++; // ensure we are not called N times
            theResult.setValue(counter);
            if (!passing) {
                registry.registerInterposedSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                        registry.setRollbackOnly();
                    }

                    @Override
                    public void afterCompletion(final int status) {
                        // no-op
                    }
                });
            }
            return theResult;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TheResult {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(final int value) {
            this.value = value;
        }
    }
}
