/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.superbiz.hello;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalObject;

/**
 * @version $Revision$ $Date$
 */
public interface HelloEjbLocal extends EJBLocalObject {

    String sayHello();

}
