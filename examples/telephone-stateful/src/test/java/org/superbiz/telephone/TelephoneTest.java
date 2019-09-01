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
package org.superbiz.telephone;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class TelephoneTest extends TestCase {

    //START SNIPPET: setup

    //Random port to avoid test conflicts
    private static final int port = Integer.parseInt(System.getProperty("ejbd.port", "" + org.apache.openejb.util.NetworkUtil.getNextAvailablePort()));

    @Override
    protected void setUp() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        //Just for this test we change the default port from 4204 to avoid conflicts
        properties.setProperty("ejbd.port", "" + port);

        // Uncomment these properties to change the defaults
        //properties.setProperty("ejbd.bind", "localhost");
        //properties.setProperty("ejbd.threads", "200");
        //properties.setProperty("ejbd.disabled", "false");
        //properties.setProperty("ejbd.only_from", "127.0.0.1,192.168.1.1");

        new InitialContext(properties);
    }
    //END SNIPPET: setup

    /**
     * Lookup the Telephone bean via its remote interface but using the LocalInitialContextFactory
     *
     * @throws Exception
     */
    //START SNIPPET: localcontext
    public void testTalkOverLocalNetwork() throws Exception {

        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        final InitialContext localContext = new InitialContext(properties);

        final Telephone telephone = (Telephone) localContext.lookup("TelephoneBeanRemote");

        telephone.speak("Did you know I am talking directly through the embedded container?");

        assertEquals("Interesting.", telephone.listen());

        telephone.speak("Yep, I'm using the bean's remote interface but since the ejb container is embedded " +
                "in the same vm I'm just using the LocalInitialContextFactory.");

        assertEquals("Really?", telephone.listen());

        telephone.speak("Right, you really only have to use the RemoteInitialContextFactory if you're in a different vm.");

        assertEquals("Oh, of course.", telephone.listen());
    }
    //END SNIPPET: localcontext

    /**
     * Lookup the Telephone bean via its remote interface using the RemoteInitialContextFactory
     *
     * @throws Exception
     */
    //START SNIPPET: remotecontext
    public void testTalkOverRemoteNetwork() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:" + port);
        final InitialContext remoteContext = new InitialContext(properties);

        final Telephone telephone = (Telephone) remoteContext.lookup("TelephoneBeanRemote");

        telephone.speak("Is this a local call?");

        assertEquals("No.", telephone.listen());

        telephone.speak("This would be a lot cooler if I was connecting from another VM then, huh?");

        assertEquals("I wondered about that.", telephone.listen());

        telephone.speak("I suppose I should hangup and call back over the LocalInitialContextFactory.");

        assertEquals("Good idea.", telephone.listen());

        telephone.speak("I'll remember this though in case I ever have to call you accross a network.");

        assertEquals("Definitely.", telephone.listen());
    }
    //END SNIPPET: remotecontext

}
