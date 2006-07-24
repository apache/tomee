/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb;

import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.BasicTimerService;

/**
 * @version $Revision$ $Date$
 */
public interface EJBInstanceContext {
    Object getId();

    Object getContainerId();

    // entity/stateful
    void associate() throws Throwable;

    // entity/stateful
    void unassociate() throws Throwable;

    // stateful
    void beforeCommit() throws Throwable;

    // stateful
    void afterCommit(boolean status) throws Throwable;

    // entity
    void flush() throws Throwable;

    // ALL beginInvocation
    boolean isInCall();

    // ALL beginInvocation
    void enter();

    // ALL endInvocation
    void exit();

    // ALL beginInvocation
    boolean isDead();

    // ALL beginInvocation
    void die();

    // ALL
    EnterpriseBean getInstance();

    // ALL
    void setOperation(EJBOperation operation);

    // replace with getDeployment().getProxyFactory()
    EJBProxyFactory getProxyFactory();

    // All
    TimerService getTimerService();

    // remove only used by internal TimerService
    BasicTimerService getBasicTimerService();

    // both of these should be handled by setOperation above
    void setTimerServiceAvailable(boolean available);
    boolean setTimerState(EJBOperation operation);

    EJBContextImpl getEJBContextImpl();

    ExtendedEjbDeployment getDeployment();

    Object getConnectorInstanceData();

    void setConnectorInstanceData(Object connectorInstanceData);
}
