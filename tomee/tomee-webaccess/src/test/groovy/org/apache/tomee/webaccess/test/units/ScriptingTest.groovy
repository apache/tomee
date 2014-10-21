/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.tomee.webaccess.test.units

import groovy.json.JsonSlurper
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.tomee.webaccess.rest.ApplicationConfig
import org.apache.tomee.webaccess.rest.Authentication
import org.apache.tomee.webaccess.rest.Scripting
import org.apache.tomee.webaccess.service.ScriptingServiceImpl
import org.apache.tomee.webaccess.test.helpers.Utilities
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(Arquillian)
class ScriptingTest {

    @ArquillianResource
    URL deploymentURL

    @Deployment
    static WebArchive createDeployment() {
        Utilities.copyFile('test/loginScript.js', 'conf/loginScript.js')
        Utilities.copyFile('test/login.config', 'conf/login.config')
        ShrinkWrap.create(WebArchive, 'webaccess.war').addClasses(
                ApplicationConfig,
                Authentication,
                Scripting,
                ScriptingServiceImpl,
                ScriptingTest
        ).addAsWebResource(new File('src/test/resources/test/context.xml'), 'META-INF/context.xml')
    }

    @Test
    void test() throws Exception {
        Utilities.withClient(deploymentURL, { CloseableHttpClient client ->
            def json = new JsonSlurper().parseText(
                    Utilities.post(deploymentURL, client, 'rest/scripting',
                            new BasicNameValuePair('engine', 'js'),
                            new BasicNameValuePair('script', 'print("Hi there!");')
                    )
            )
            Assert.assertEquals(
                    'Hi there!'.trim(),
                    (json.output as String).trim()
            )
            def result = Utilities.post(deploymentURL, client, 'rest/scripting',
                    new BasicNameValuePair('engine', 'js'),
                    new BasicNameValuePair('script', 'throw "my bad!";')
            )
            Assert.assertTrue("Expecting to have a 'javax.script.ScriptException'. Resulting message: ${result}",
                    result.contains('javax.script.ScriptException'))
            Assert.assertTrue("Expecting to have the 'my bad!' message. Resulting message: ${result}",
                    result.contains('my bad!'))
        })
    }

}
