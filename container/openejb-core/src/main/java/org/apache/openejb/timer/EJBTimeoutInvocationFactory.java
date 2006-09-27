package org.apache.openejb.timer;

import javax.ejb.Timer;

import org.apache.openejb.EjbInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public interface EJBTimeoutInvocationFactory {

    EjbInvocation getEJBTimeoutInvocation(Object id, Timer timer);

}
