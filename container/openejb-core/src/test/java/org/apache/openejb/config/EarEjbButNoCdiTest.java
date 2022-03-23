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
package org.apache.openejb.config;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// not fully spec compliant but save a bunch of mem for legacy apps
// + avoid to breaks existing ones so better than the opposite
// we support config for that anyway
@RunWith(ApplicationComposer.class)
public class EarEjbButNoCdiTest {
    @Module
    public EjbJar ejb() {
        return new EjbJar()
                .enterpriseBean(new StatelessBean(B1.class));
    }

    @Module
    public WebApp web() { // we have shortcut when we have a single module so adding another one ensure we test an "ear"
        return new WebApp();
    }

    @EJB
    private B1 b1;

    @Test
    public void check() {
        assertEquals("1", b1.val());
        try {
            WebBeansContext.currentInstance();
            fail();
        } catch (final IllegalStateException ise) {
            // ok
        }
    }

    @Stateless
    public static class B1 {
        public String val() {
            return "1";
        }
    }
}
