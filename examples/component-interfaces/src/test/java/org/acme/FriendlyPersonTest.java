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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * 
 * @version $Rev$ $Date$
 */
public class FriendlyPersonTest extends TestCase {
    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        initialContext = new InitialContext(properties);
    }

    /**
     * Here we lookup and test the FriendlyPerson bean via its EJB 2.1 EJBHome and EJBObject interfaces
     *
     * @throws Exception
     */
    public void testEjbHomeAndEjbObject() throws Exception {
        Object object = initialContext.lookup("FriendlyPersonRemoteHome");
        FriendlyPersonEjbHome home = (FriendlyPersonEjbHome) object;
        FriendlyPersonEjbObject friendlyPerson = home.create();

        friendlyPerson.setDefaultLanguage("en");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));

        friendlyPerson.setLanguagePreferences("Amelia", "es");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));

        // Amelia took some French, let's see if she remembers
        assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));

        // Dave should take some Polish and if he had, he could say Hi in Polish
        assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));

        // Let's see if I speak Portuguese
        assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));

        // Ok, well I've been meaning to learn, so...
        friendlyPerson.addGreeting("pt", "Ola {0}!");

        assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
    }


    /**
     * Here we lookup and test the FriendlyPerson bean via its EJB 2.1 EJBLocalHome and EJBLocalObject interfaces
     *
     * @throws Exception
     */
    public void testEjbLocalHomeAndEjbLocalObject() throws Exception {
        Object object = initialContext.lookup("FriendlyPersonLocalHome");
        FriendlyPersonEjbLocalHome home = (FriendlyPersonEjbLocalHome) object;
        FriendlyPersonEjbLocalObject friendlyPerson = home.create();

        friendlyPerson.setDefaultLanguage("en");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));

        friendlyPerson.setLanguagePreferences("Amelia", "es");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));

        // Amelia took some French, let's see if she remembers
        assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));

        // Dave should take some Polish and if he had, he could say Hi in Polish
        assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));

        // Let's see if I speak Portuguese
        assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));

        // Ok, well I've been meaning to learn, so...
        friendlyPerson.addGreeting("pt", "Ola {0}!");

        assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
    }

    /**
     * Here we lookup and test the FriendlyPerson bean via its EJB 3.0 business remote interface
     *
     * @throws Exception
     */
    public void testBusinessRemote() throws Exception {
        Object object = initialContext.lookup("FriendlyPersonRemote");

        FriendlyPersonRemote friendlyPerson = (FriendlyPersonRemote) object;

        friendlyPerson.setDefaultLanguage("en");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));

        friendlyPerson.setLanguagePreferences("Amelia", "es");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));

        // Amelia took some French, let's see if she remembers
        assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));

        // Dave should take some Polish and if he had, he could say Hi in Polish
        assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));

        // Let's see if I speak Portuguese
        assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));

        // Ok, well I've been meaning to learn, so...
        friendlyPerson.addGreeting("pt", "Ola {0}!");

        assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
    }

    /**
     * Here we lookup and test the FriendlyPerson bean via its EJB 3.0 business local interface
     *
     * @throws Exception
     */
    public void testBusinessLocal() throws Exception {
        Object object = initialContext.lookup("FriendlyPersonLocal");

        FriendlyPersonLocal friendlyPerson = (FriendlyPersonLocal) object;

        friendlyPerson.setDefaultLanguage("en");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hello Amelia!", friendlyPerson.greet("Amelia"));

        friendlyPerson.setLanguagePreferences("Amelia", "es");

        assertEquals("Hello David!", friendlyPerson.greet("David"));
        assertEquals("Hola Amelia!", friendlyPerson.greet("Amelia"));

        // Amelia took some French, let's see if she remembers
        assertEquals("Bonjour Amelia!", friendlyPerson.greet("fr", "Amelia"));

        // Dave should take some Polish and if he had, he could say Hi in Polish
        assertEquals("Witaj Dave!", friendlyPerson.greet("pl", "Dave"));

        // Let's see if I speak Portuguese
        assertEquals("Sorry, I don't speak " + new Locale("pt").getDisplayLanguage() + ".", friendlyPerson.greet("pt", "David"));

        // Ok, well I've been meaning to learn, so...
        friendlyPerson.addGreeting("pt", "Ola {0}!");

        assertEquals("Ola David!", friendlyPerson.greet("pt", "David"));
    }


}
