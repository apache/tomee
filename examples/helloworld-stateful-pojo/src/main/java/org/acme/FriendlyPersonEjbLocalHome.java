/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import java.rmi.RemoteException;

/**
 * @version $Revision$ $Date$
 */
public interface FriendlyPersonEjbLocalHome extends EJBLocalHome {
    FriendlyPersonEjbLocalObject create() throws CreateException, RemoteException;
}
