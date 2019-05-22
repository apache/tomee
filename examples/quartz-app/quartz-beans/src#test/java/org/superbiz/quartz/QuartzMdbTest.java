/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.quartz;

import org.apache.openejb.config.OutputGeneratedDescriptors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Properties;

public class QuartzMdbTest {

    private static InitialContext initialContext = null;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(OutputGeneratedDescriptors.OUTPUT_DESCRIPTORS, "false"); // just to avoid to dump files in /tmp
        if (null == initialContext) {
            Properties properties = new Properties();
            properties.setProperty("openejb.deployments.classpath.include", ".*quartz-.*");
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

            initialContext = new InitialContext(properties);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (null != initialContext) {
            initialContext.close();
            initialContext = null;
        }
    }

    @Test
    public void testLookup() throws Exception {

        final JobScheduler jbi = (JobScheduler) initialContext.lookup("JobBeanLocal");
        final Date d = jbi.createJob();
        Thread.sleep(500);
        System.out.println("Scheduled test job should have run at: " + d.toString());
    }

    @Test
    public void testMdb() throws Exception {
        // Sleep 3 seconds and give quartz a chance to execute our MDB
        Thread.sleep(3000);
    }
}
