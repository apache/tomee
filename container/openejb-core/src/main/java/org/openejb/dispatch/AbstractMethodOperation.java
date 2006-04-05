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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.geronimo.interceptor.InvocationResult;

import net.sf.cglib.reflect.FastClass;

import org.openejb.EJBInstanceContext;
import org.openejb.EjbInvocation;
import org.openejb.EJBOperation;
import org.openejb.timer.TimerState;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractMethodOperation implements VirtualOperation, Serializable {
    private final Class beanClass;
    private final MethodSignature signature;

    private transient FastClass fastClass;
    private transient int methodIndex;

    public AbstractMethodOperation(Class beanClass, MethodSignature signature) {
        this.beanClass = beanClass;
        this.signature = signature;
        initializeCGLIBFields();
    }

    protected InvocationResult invoke(EjbInvocation invocation, EJBOperation operation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();
        boolean oldTimerMethodAvailable = ctx.setTimerState(operation);
        try {
            ctx.setOperation(operation);
            try {
                return invocation.createResult(fastClass.invoke(methodIndex, ctx.getInstance(), invocation.getArguments()));
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException();
                if (t instanceof Exception && t instanceof RuntimeException == false) {
                    // checked exception - which we simply include in the result
                    return invocation.createExceptionResult((Exception)t);
                } else {
                    // unchecked Exception - just throw it to indicate an abnormal completion
                    throw t;
                }
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }
    }

    private void initializeCGLIBFields() {
        fastClass = FastClass.create(beanClass);
        Method javaMethod = signature.getMethod(beanClass);
        if (javaMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement method:" +
                    " beanClass=" + beanClass.getName() + " method=" + signature);
        }
        methodIndex = fastClass.getIndex(javaMethod.getName(), javaMethod.getParameterTypes());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializeCGLIBFields();
    }

}
