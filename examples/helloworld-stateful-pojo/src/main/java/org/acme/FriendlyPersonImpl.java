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
public class FriendlyPersonImpl implements FriendlyPerson {

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

    public void setSessionContext(Object object){
        // This is required until this bug is fixed http://jira.codehaus.org/browse/OPENEJB-259
    }
}
