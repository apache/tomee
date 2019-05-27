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

import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.server.cxf.fault.AuthenticatorServiceBean;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class GlobalFeatureConfigTest {
    @Configuration
    public Properties p() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");

            setProperty(CxfUtil.BUS_PREFIX + CxfUtil.FEATURES, "logging");
            setProperty("logging", "new://Service?class-name=" + LoggingFeature.class.getName());
        }};
    }

    @Module
    public StatelessBean bean() {
        return (StatelessBean) new StatelessBean(AuthenticatorServiceBean.class).localBean();
    }

    @Test
    public void run() {
        final Bus bus = CxfUtil.getBus();
        assertEquals(1, bus.getFeatures().size());
        assertThat(bus.getFeatures().iterator().next(), instanceOf(LoggingFeature.class));
    }
}
