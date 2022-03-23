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

package org.apache.openejb.util.proxy;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.LocalHome;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class LocalReferenceSerializationTest {


    @EJB
    private RedLocal red;

    @EJB
    private OrangeLocalBean orange;

    @EJB
    private GreenLocalHome greenHome;

    @Module
    public EjbJar app() {
        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(OrangeLocalBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(RedBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(GreenBean.class));

        return ejbJar;
    }

    @Test
    public void testLocalBean() throws Exception {
        assertEquals(5, copy(orange).add(2, 3));
    }

    @Test
    public void testLocal() throws Exception {
        assertEquals(5, copy(red).add(2, 3));
    }

    @Test
    public void testLocalObject() throws Exception {
        assertEquals(5, greenHome.create().add(2, 3));


        final GreenLocal greenLocal = copy(greenHome).create();

        assertEquals(5, copy(greenLocal).add(2, 3));
    }

    private static <T> T copy(final T bean) throws IOException, ClassNotFoundException {
        assertNotNull(bean);

        ServerFederation.setApplicationServer(new TestApplicationServer());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(bean);

        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);
        return (T) ois.readObject();
    }

    @LocalBean
    public static class OrangeLocalBean {

        public int add(final int a, final int b) {
            return a + b;
        }
    }

    @Local
    public static class RedBean implements RedLocal {

        public int add(final int a, final int b) {
            return a + b;
        }
    }

    public static interface RedLocal {
        public int add(int a, int b);
    }

    @LocalHome(GreenLocalHome.class)
    public static class GreenBean {

        public int add(final int a, final int b) {
            return a + b;
        }
    }

    public static interface GreenLocal extends EJBLocalObject {
        public int add(int a, int b);
    }

    public static interface GreenLocalHome extends EJBLocalHome {
        public GreenLocal create() throws CreateException;
    }


    private static class TestApplicationServer implements ApplicationServer {
        @Override
        public EJBMetaData getEJBMetaData(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }

        @Override
        public Handle getHandle(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }

        @Override
        public HomeHandle getHomeHandle(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }

        @Override
        public EJBObject getEJBObject(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }

        @Override
        public Object getBusinessObject(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }

        @Override
        public EJBHome getEJBHome(final ProxyInfo proxyInfo) {
            throw new AssertionError("Should never be called");
        }
    }
}
