/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.acme;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
 * @version $Revision$ $Date$
 */
public interface FriendlyPersonEjbHome extends EJBHome {
    FriendlyPersonEjbObject create() throws CreateException, RemoteException;
}
