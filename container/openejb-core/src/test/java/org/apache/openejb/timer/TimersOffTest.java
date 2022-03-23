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
package org.apache.openejb.timer;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class TimersOffTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder().p("openejb.timers.on", "false").build();
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(TimerBean.class).localBean();
    }

    @EJB
    private TimerBean bean;

    @Test
    public void checkOff() throws InterruptedException {
        Thread.sleep(2000);
        assertEquals(0, bean.getCount());
    }

    public static class TimerBean {
        private int count = 0;

        @Schedule(hour = "*", minute = "*", second = "*")
        public void inc() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }
}
