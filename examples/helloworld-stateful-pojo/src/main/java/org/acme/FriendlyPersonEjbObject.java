/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme;

import javax.ejb.Remote;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;

@Remote
public interface FriendlyPersonEjbObject extends EJBObject {
    String greet(String friend) throws RemoteException;

    String greet(String language, String friend)  throws RemoteException;

    void addGreeting(String language, String message) throws RemoteException;

    void setLanguagePreferences(String friend, String language) throws RemoteException;

    String getDefaultLanguage() throws RemoteException;

    void setDefaultLanguage(String defaultLanguage) throws RemoteException;

}
