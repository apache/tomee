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

import javax.ejb.Init;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.ejb.Local;
import javax.ejb.RemoteHome;
import javax.ejb.LocalHome;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

/**
 * This is an EJB 3 style pojo stateful session bean
 * it does not need to implement javax.ejb.SessionBean
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */

// EJB 3.0 Style business interfaces
// Each of these interfaces are already annotated in the classes
// themselves with @Remote and @Local, so annotating them here
// in the bean class again is not really required.
@Remote({FriendlyPersonRemote.class})
@Local({FriendlyPersonLocal.class})

// EJB 2.1 Style component interfaces
// These interfaces, however, must be annotated here in the bean class.
// Use of @RemoteHome in the FriendlyPersonEjbHome class itself is not allowed.
// Use of @LocalHome in the FriendlyPersonEjbLocalHome class itself is also not allowed.
@RemoteHome(FriendlyPersonEjbHome.class)
@LocalHome(FriendlyPersonEjbLocalHome.class)

@Stateful(name="FriendlyPerson")
public class FriendlyPersonImpl implements FriendlyPersonLocal, FriendlyPersonRemote {

    private final HashMap<String, MessageFormat> greetings;
    private final Properties languagePreferences;

    private String defaultLanguage;

    public FriendlyPersonImpl() {
        greetings = new HashMap();
        languagePreferences = new Properties();
        defaultLanguage = Locale.getDefault().getLanguage();

        addGreeting("en", "Hello {0}!");
        addGreeting("es", "Hola {0}!");
        addGreeting("fr", "Bonjour {0}!");
        addGreeting("pl", "Witaj {0}!");
    }

    /**
     * This method corresponds to the FriendlyPersonEjbHome.create() method
     * and the FriendlyPersonEjbLocalHome.create()
     *
     * If you do not have an EJBHome or EJBLocalHome interface, this method
     * can be deleted.
     */
    @Init
    public void create(){}

    /**
     * This method corresponds to the following methods:
     *  - EJBObject.remove()
     *  - EJBHome.remove(ejbObject)
     *  - EJBLocalObject.remove()
     *  - EJBLocalHome.remove(ejbObject)
     *
     * If you do not have an EJBHome or EJBLocalHome interface, this method
     * can be deleted.
     */
    @Remove
    public void remove(){}

    public String greet(String friend) {
        String language = languagePreferences.getProperty(friend, defaultLanguage);
        return greet(language, friend);
    }

    public String greet(String language, String friend) {
        MessageFormat greeting = greetings.get(language);
        if (greeting == null) {
            Locale locale = new Locale(language);
            return "Sorry, I don't speak " + locale.getDisplayLanguage() + ".";
        }

        return greeting.format(new Object[]{friend});
    }

    public void addGreeting(String language, String message) {
        greetings.put(language, new MessageFormat(message));
    }

    public void setLanguagePreferences(String friend, String language) {
        languagePreferences.put(friend, language);
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
