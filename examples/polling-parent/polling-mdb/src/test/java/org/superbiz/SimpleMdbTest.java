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
package org.superbiz;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.mdb.Api;
import org.superbiz.mdb.ApiLog;
import org.superbiz.mdb.CounterBean;
import org.superbiz.mdb.LogMdb;

import javax.ejb.EJB;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(Arquillian.class)
public class SimpleMdbTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Api.class, ApiLog.class, LogMdb.class, CounterBean.class)
                .addAsResource(new ClassLoaderAsset("META-INF/beans.xml"), "META-INF/beans.xml")
                .addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml"), "META-INF/ejb-jar.xml");
    }

    @ArquillianResource
    private URL deploymentUrl;

    @EJB
    private CounterBean logs;

    @Test
    public void testDataSourceOne() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 200; i++) {
            executor.submit(new CallGet(this.deploymentUrl, i));
        }
        executor.shutdown();
        Assert.assertTrue("Unable to execute all the GET calls", executor.awaitTermination(10, TimeUnit.SECONDS));
        Map<Integer, AtomicInteger> expected = new TreeMap<>();
        expected.put(1, new AtomicInteger(20));
        expected.put(2, new AtomicInteger(20));
        expected.put(3, new AtomicInteger(20));
        expected.put(4, new AtomicInteger(20));
        expected.put(5, new AtomicInteger(20));
        expected.put(6, new AtomicInteger(20));
        expected.put(7, new AtomicInteger(20));
        expected.put(8, new AtomicInteger(20));
        expected.put(9, new AtomicInteger(20));
        expected.put(10, new AtomicInteger(20));
        for(int i = 0; i < 10; i++) {
            if (expected.toString().equals(logs.getUsage().toString())) {
                break;
            }
            Thread.sleep(1000);
        }
        Assert.assertEquals(expected.toString(), logs.getUsage().toString());
    }

    private class CallGet implements Runnable {
        private final URL url;
        private final int index;

        private CallGet(URL url, int index) {
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.toURI().resolve("log/lala_" + index).toURL().openConnection();
                conn.setRequestMethod("GET");
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    while (rd.readLine() != null) {
                        // ignore
                    }
                }
            } catch (IOException | URISyntaxException e) {
                // ignore
            }
        }
    }
}
