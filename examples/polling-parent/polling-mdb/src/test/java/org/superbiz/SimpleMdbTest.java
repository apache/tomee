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
import org.superbiz.mdb.LogsBean;

import javax.ejb.EJB;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(Arquillian.class)
public class SimpleMdbTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(Api.class.getPackage())
                .addAsResource(new ClassLoaderAsset("META-INF/beans.xml"), "META-INF/beans.xml")
                .addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml"), "META-INF/ejb-jar.xml");
    }

    @ArquillianResource
    private URL baseURL;

    @EJB
    private LogsBean logs;

    @Test
    public void testDataSourceOne() throws Exception {
        final Client client = ClientBuilder.newClient();
        for (int i = 0; i < 20; i++) {
            client.target(baseURL.toExternalForm())
                    .request("log/lala_" + i)
                    .get();
        }
        Thread.sleep(2000);
        List<String> expected = new ArrayList<>();
        expected.add("BEAN_1 [7] -> lala_61");
        List<String> actual = this.logs.getMessages();
        Collections.sort(expected);
        Collections.sort(actual);
        Assert.assertEquals(expected, actual);
    }

}
