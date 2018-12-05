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
package org.apache.openejb.cdi;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.portable.events.discovery.AfterBeanDiscoveryImpl;
import org.apache.webbeans.portable.events.discovery.AfterDeploymentValidationImpl;
import org.apache.webbeans.portable.events.discovery.BeforeBeanDiscoveryImpl;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.portable.events.generics.GProcessAnnotatedType;
import org.apache.webbeans.portable.events.generics.GProcessBean;
import org.apache.webbeans.portable.events.generics.GProcessInjectionTarget;
import org.apache.webbeans.portable.events.generics.GProcessProducer;
import org.apache.webbeans.portable.events.generics.GProcessProducerField;
import org.apache.webbeans.portable.events.generics.GProcessProducerMethod;
import org.apache.webbeans.portable.events.generics.GProcessSessionBean;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.fail;

public class WebappBeanManagerTest {
    @Test
    public void containerEventsShouldntGoUp() {
        final WebappWebBeansContext ctx = new WebappWebBeansContext(Collections.<Class<?>, Object>emptyMap(), new Properties(), new WebBeansContext());
        final WebappBeanManager wbm = new WebappBeanManager(ctx) {
            @Override
            public BeanManagerImpl getParentBm() {
                throw new IllegalStateException("shouldn't be called");
            }
        };
        wbm.fireEvent(new GProcessProducer(null, null), true);
        wbm.fireEvent(new GProcessProducerField(null, null, null), true);
        wbm.fireEvent(new GProcessProducerMethod(null, null, null), true);
        wbm.fireEvent(new GProcessInjectionTarget(null, null), true);
        wbm.fireEvent(new GProcessBean(null, null), true);
        wbm.fireEvent(new GProcessAnnotatedType(ctx, null), true);
        wbm.fireEvent(new GProcessSessionBean(null, null, null, null), true);
        wbm.fireEvent(new AfterBeanDiscoveryImpl(ctx), true);
        wbm.fireEvent(new AfterDeploymentValidationImpl(wbm), true);
        wbm.fireEvent(new BeforeBeanDiscoveryImpl(ctx), true);
        wbm.fireEvent(new BeforeShutdownImpl(), true);
    }

    @Test
    public void otherEventsShouldGoUp() {
        final WebappWebBeansContext ctx = new WebappWebBeansContext(Collections.<Class<?>, Object>emptyMap(), new Properties(), new WebBeansContext());
        final WebappBeanManager wbm = new WebappBeanManager(ctx) {
            @Override
            public BeanManagerImpl getParentBm() {
                throw new IllegalStateException("shouldn't be called");
            }
        };
        try {
            wbm.fireEvent(new Object());
            fail();
        } catch (final IllegalStateException ise) {
            // ok
        }
        try {
            wbm.fireEvent("yeah");
            fail();
        } catch (final IllegalStateException ise) {
            // ok
        }
        try {
            wbm.fireEvent(this);
            fail();
        } catch (final IllegalStateException ise) {
            // ok
        }
    }
}
