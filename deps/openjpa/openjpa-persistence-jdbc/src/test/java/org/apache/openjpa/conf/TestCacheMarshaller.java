/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.conf;

import org.apache.openjpa.conf.CacheMarshaller.ValidationPolicy;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCacheMarshaller
    extends SingleEMFTestCase {

    private CacheMarshaller cm;

    public void setUp() {
        setUp(new Object[] { "openjpa.CacheMarshallers",
            "default(Id=" + getClass().getName() + ", ValidationPolicy="
                + OpenJPAVersionAndConfigurationTypeValidationPolicy.class
                    .getName()
                + ", InputURL=file:target/test-classes/" 
                + getClass().getName() + ".ser"
                + ", OutputFile=target/test-classes/"
                + getClass().getName() + ".ser)"
        });
        emf.createEntityManager().close();
        cm = CacheMarshallersValue.getMarshallerById(emf.getConfiguration(),
            getClass().getName());
    }

    public void testCacheMarshallerType() {
        assertEquals(CacheMarshallerImpl.class, cm.getClass());
    }

    public void testConfiguration() {
        assertEquals(getClass().getName(), cm.getId());
    }

    public void testValidation() {
        ValidationPolicy vp = ((CacheMarshallerImpl) cm).getValidationPolicy();
        assertEquals(OpenJPAVersionAndConfigurationTypeValidationPolicy.class,
            vp.getClass());
        Object[] cached = (Object[]) vp.getCacheableData(this);

        assertEquals(3, cached.length);
        assertEquals(OpenJPAVersion.VERSION_ID, cached[0]);
        assertEquals(emf.getConfiguration().getClass().getName(), cached[1]);
        assertEquals(this, cached[2]);

        assertEquals(this, vp.getValidData(cached));
    }

    public void testRoundTrip() {
        Object o = "foo";
        cm.store(o);
        assertEquals(o, cm.load());
    }
    
    public void testendConfiguration() {
        ((CacheMarshallerImpl)cm).setInputURL(null);
        ((CacheMarshallerImpl)cm)
            .setInputResource(getClass().getName() + ".ser");
        ((CacheMarshallerImpl)cm).endConfiguration();
    }
}
