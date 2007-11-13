/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.superbiz.hello;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

/**
 * @version $Revision$ $Date$
 */
public interface HelloEjbLocalHome extends EJBLocalHome {
    HelloEjbLocal create() throws CreateException;
}
