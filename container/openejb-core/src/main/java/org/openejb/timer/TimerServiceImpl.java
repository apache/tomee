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
package org.openejb.timer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.openejb.EJBInstanceContext;


/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerServiceImpl implements TimerService {

    private final EJBInstanceContext context;

    public TimerServiceImpl(EJBInstanceContext context) {
        this.context = context;
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), initialExpiration, intervalDuration, info);
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), expiration, info);
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), initialDuration, intervalDuration, info);
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), duration, info);
    }

    public Collection getTimers() throws IllegalStateException, EJBException {
        checkState();
        //TODO this check is here because entity bean remove calls this to get the list of timers to cancel.
        //Possibly there is a better place to check that the entity bean is a timed object.
        return context.getBasicTimerService() == null? Collections.EMPTY_SET: context.getBasicTimerService().getTimers(context.getId());
    }

    private void checkState() throws IllegalStateException {
        if (!TimerState.getTimerState()) {
            throw new IllegalStateException("Timer methods not available");
        }
    }
}
