package org.openejb.timer;

import javax.ejb.Timer;

import org.openejb.EjbInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public interface EJBTimeoutInvocationFactory {

    EjbInvocation getEJBTimeoutInvocation(Object id, Timer timer);

}
