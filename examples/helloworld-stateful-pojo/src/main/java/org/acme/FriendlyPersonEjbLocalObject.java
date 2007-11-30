/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme;

import javax.ejb.Remote;
import javax.ejb.EJBLocalObject;

@Remote
public interface FriendlyPersonEjbLocalObject extends EJBLocalObject {
    String greet(String friend);

    String greet(String language, String friend);

    void addGreeting(String language, String message);

    void setLanguagePreferences(String friend, String language);

    String getDefaultLanguage();

    void setDefaultLanguage(String defaultLanguage);

}
