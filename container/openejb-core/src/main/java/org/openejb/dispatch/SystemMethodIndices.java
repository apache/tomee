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
package org.openejb.dispatch;

import javax.ejb.Timer;

import org.openejb.EJBInstanceContext;
import org.openejb.EjbInvocation;
import org.openejb.EjbInvocationImpl;
import org.openejb.EJBInterfaceType;
import org.openejb.timer.EJBTimeoutInvocationFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public final class SystemMethodIndices implements EJBTimeoutInvocationFactory {

    private final int ejbActivate;
    private final int ejbLoad;
    private final int ejbPassivate;
    private final int ejbStore;
    private final int ejbCreate;
    private final int ejbRemove;
    private final int ejbTimeout;
    private final int setContext;
    private final int unsetContext;
    private final int afterBegin;
    private final int beforeCompletion;
    private final int afterCompletion;

    public static SystemMethodIndices createSystemMethodIndices(InterfaceMethodSignature[] signatures, String setContextName, String setContextType, String unsetContextName) {
        int ejbActivate = -1;
        int ejbLoad = -1;
        int ejbPassivate = -1;
        int ejbStore = -1;
        int ejbTimeout = -1;
        int ejbCreate = -1;
        int ejbRemove = -1;
        int setContext = -1;
        int unsetContext = -1;
        int afterBegin = -1;
        int beforeCompletion = -1;
        int afterCompletion = -1;
        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            if (signature.getMethodName().equals("ejbActivate")) {
                 ejbActivate = i;
            } else if (signature.getMethodName().equals("ejbLoad")) {
                 ejbLoad = i;
            } else if (signature.getMethodName().equals("ejbPassivate")) {
                 ejbPassivate = i;
            } else if (signature.getMethodName().equals("ejbStore")) {
                 ejbStore = i;
            } else if (signature.getMethodName().equals("ejbTimeout")) {
                 ejbTimeout = i;
            } else if (signature.getMethodName().equals("ejbCreate") && signature.getParameterTypes().length == 0 && !signature.isHomeMethod() )  {
                 ejbCreate = i;
            } else if (signature.getMethodName().equals("ejbRemove") && signature.getParameterTypes().length == 0 && !signature.isHomeMethod() ) {
                 ejbRemove = i;
            } else if (signature.getMethodName().equals(setContextName) && signature.getParameterTypes().length == 1 && signature.getParameterTypes()[0].equals(setContextType)) {
                 setContext = i;
            } else if (signature.getMethodName().equals(unsetContextName) && signature.getParameterTypes().length == 0) {
                 unsetContext = i;
            } else if (signature.getMethodName().equals("afterBegin") && signature.getParameterTypes().length == 0 && !signature.isHomeMethod() ) {
                 afterBegin = i;
            } else if (signature.getMethodName().equals("beforeCompletion") && signature.getParameterTypes().length == 0 && !signature.isHomeMethod() ) {
                 beforeCompletion = i;
            } else if (signature.getMethodName().equals("afterCompletion") && signature.getParameterTypes().length == 1 && !signature.isHomeMethod() && signature.getParameterTypes()[0].equals(boolean.class.getName())) {
                 afterCompletion = i;
            }
        }
        return new SystemMethodIndices(ejbActivate, ejbLoad, ejbPassivate, ejbStore, ejbTimeout, ejbCreate, ejbRemove, setContext, unsetContext, afterBegin, beforeCompletion, afterCompletion);
    }

    public SystemMethodIndices(int ejbActivate, int ejbLoad, int ejbPassivate, int ejbStore, int ejbTimeout, int ejbCreate, int ejbRemove, int setContext, int unsetContext, int afterBegin, int beforeCompletion, int afterCompletion) {
        this.ejbActivate = ejbActivate;
        this.ejbLoad = ejbLoad;
        this.ejbPassivate = ejbPassivate;
        this.ejbStore = ejbStore;
        this.ejbTimeout = ejbTimeout;
        this.ejbCreate = ejbCreate;
        this.ejbRemove = ejbRemove;
        this.setContext = setContext;
        this.unsetContext = unsetContext;
        this.afterBegin = afterBegin;
        this.beforeCompletion = beforeCompletion;
        this.afterCompletion = afterCompletion;
    }

    public EjbInvocation getEjbActivateInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbActivate, null, instanceContext);
    }

    public EjbInvocation getEjbLoadInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbLoad, null, instanceContext);
    }

    public EjbInvocation getEjbPassivateInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbPassivate, null, instanceContext);
    }

    public EjbInvocation getEjbStoreInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbStore, null, instanceContext);
    }

    public EjbInvocation getEJBTimeoutInvocation(Object id, Timer timer) {
        return new EjbInvocationImpl(EJBInterfaceType.TIMEOUT, id, ejbTimeout, new Object[] {timer});
    }

    public EjbInvocation getEJBCreateInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbCreate, null, instanceContext);
    }

    public EjbInvocation getEJBRemoveInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(ejbRemove, null, instanceContext);

    }

    public EjbInvocation getSetContextInvocation(EJBInstanceContext instanceContext, Object context) {
        return new EjbInvocationImpl(setContext, new Object[] {context}, instanceContext);
    }

    public EjbInvocation getUnsetContextInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(unsetContext, null, instanceContext);
    }

    public EjbInvocation getAfterBeginInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(afterBegin, null, instanceContext);
    }

    public EjbInvocation getBeforeCompletionInvocation(EJBInstanceContext instanceContext) {
        return new EjbInvocationImpl(beforeCompletion, null, instanceContext);
    }

    public EjbInvocation getAfterCompletionInvocation(EJBInstanceContext instanceContext, boolean comitted) {
        return new EjbInvocationImpl(afterCompletion, new Object[]{Boolean.valueOf(comitted)}, instanceContext);
    }
}
