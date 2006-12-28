/**
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
package org.apache.openejb.examples.telephone;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class TelephoneTest extends TestCase {

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*remotable.*");
        properties.setProperty("openejb.embedded.remotable", "true");

        // We really don't need to keep this, though it's fine and even encouraged to do so
        // however we will toss it aside and create another one later to demonstrate the
        // embedded nature and the ways to access the embedded container system
        // locally in this vm and remotely over a network.
        new InitialContext(properties);
    }

    /**
     * Lookup the Calculator bean via its remote home interface
     *
     * @throws Exception
     */
    public void testTalkOverLocalNetwork() throws Exception {

        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        InitialContext localContext = new InitialContext(properties);

        Object object = localContext.lookup("TelephoneBeanBusinessRemote");

        assertNotNull(object);
        assertTrue(object instanceof Telephone);
        Telephone telephone = (Telephone) object;

        telephone.speak("Did you know I am talking directly through the embedded container?");

        assertEquals("Interesting.", telephone.listen());


        telephone.speak("Yep, I'm using the bean's remote interface but since the ejb container is embedded in the same vm I'm just using the LocalInitialContextFactory.");

        assertEquals("Really?", telephone.listen());


        telephone.speak("Right, you really only have to use the RemoteInitialContextFactory if you're in a different vm.");

        assertEquals("Oh, of course.", telephone.listen());


    }

    /**
     * Lookup the Calculator bean via its local home interface
     *
     * @throws Exception
     */
    public void testTalkOverRemoteNetwork() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        InitialContext remoteContext = new InitialContext(properties);

        Object object = remoteContext.lookup("TelephoneBeanBusinessRemote");

        assertNotNull(object);
        assertTrue(object instanceof Telephone);
        Telephone telephone = (Telephone) object;

        telephone.speak("Is this a local call?");

        assertEquals("No.", telephone.listen());


        telephone.speak("This would be a lot cooler if I was connecting from another VM then, huh?");

        assertEquals("I wondered about that.", telephone.listen());


        telephone.speak("I suppose I should hangup and call back over the LocalInitialContextFactory.");

        assertEquals("Good idea.", telephone.listen());


        telephone.speak("I'll remember this though in case I ever have to call you accross a network.");

        assertEquals("Definitely.", telephone.listen());
    }

}
