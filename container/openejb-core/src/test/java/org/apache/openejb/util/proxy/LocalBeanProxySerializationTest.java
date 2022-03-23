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

import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class LocalBeanProxySerializationTest {
    @EJB
    private SampleLocalBean bean;

    @Module
    public StatelessBean app() {
        final StatelessBean bean = new StatelessBean(SampleLocalBean.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Test
    public void testSerialization() throws Exception {
        assertNotNull(bean);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(bean);

        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStreamFiltered(bis);
        final SampleLocalBean deserialized = (SampleLocalBean) ois.readObject();
        assertEquals(5, deserialized.add(2, 3));
    }
}
