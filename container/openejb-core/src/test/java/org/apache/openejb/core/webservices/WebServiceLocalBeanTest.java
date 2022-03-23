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
package org.apache.openejb.core.webservices;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class WebServiceLocalBeanTest {
    @EJB
    private WebServiceImplicitLocalBean wslb;

    @EJB
    private WSLocal wsl;

    @Module
    public Class<?>[] classes() {
        return new Class<?>[]{WSLocal.class, WebServiceLocal.class, WebServiceImplicitLocalBean.class};
    }

    @Test
    public void checkLocalBean() {
        assertNotNull(wslb);
        assertEquals("ok", wslb.str());
    }

    @Test
    public void checkLocal() {
        assertNotNull(wsl);
        assertEquals("ok2", wsl.str());
    }

    @Stateless
    @WebService
    public static class WebServiceImplicitLocalBean {
        public String str() {
            return "ok";
        }
    }

    @Stateless
    @WebService
    public static class WebServiceLocal implements WSLocal {
        public String str() {
            return "ok2";
        }
    }

    //@WebService
    public static interface WSLocal {
        String str();
    }
}
