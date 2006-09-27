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
package org.apache.openejb.entity.bmp;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.TimerState;
import org.apache.openejb.util.SerializableEnumeration;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BmpFinderMethod implements VirtualOperation, Serializable  {
    private final Class beanClass;
    private final MethodSignature finderSignature;

    private transient FastClass fastClass;
    private transient int finderIndex;

    public BmpFinderMethod(Class beanClass, MethodSignature finderSignature) {
        this.beanClass = beanClass;
        this.finderSignature = finderSignature;

        fastClass = FastClass.create(beanClass);
        Method javaMethod = finderSignature.getMethod(beanClass);
        if(javaMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement finder method:" +
                    " beanClass=" + beanClass.getName() + " method=" + finderSignature);
        }
        finderIndex = fastClass.getIndex(javaMethod.getName(), javaMethod.getParameterTypes());
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();

        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        Object finderResult;
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBFIND);
        try {
            ctx.setOperation(EJBOperation.EJBFIND);
            finderResult = fastClass.invoke(finderIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        boolean local = invocation.getType().isLocal();
        EJBProxyFactory proxyFactory = ctx.getProxyFactory();

        if (finderResult instanceof Enumeration) {
            Enumeration e = (Enumeration) finderResult;
            ArrayList values = new ArrayList();
            while (e.hasMoreElements()) {
                values.add(getReference(local, proxyFactory, e.nextElement()));
            }
            return invocation.createResult(new SerializableEnumeration(values.toArray()));
        } else if (finderResult instanceof Collection) {
            Collection c = (Collection) finderResult;
            ArrayList result = new ArrayList(c.size());
            for (Iterator i = c.iterator(); i.hasNext();) {
                result.add(getReference(local, proxyFactory, i.next()));
            }
            return invocation.createResult(result);
        } else {
            return invocation.createResult(getReference(local, proxyFactory, finderResult));
        }
    }

    private Object getReference(boolean local, EJBProxyFactory proxyFactory, Object id) {
        if (id == null) {
            // yes, finders can return null
            return null;
        } else if (local) {
            return proxyFactory.getEJBLocalObject(id);
        } else {
            return proxyFactory.getEJBObject(id);
        }
    }

    private Object readResolve() {
        return new BmpFinderMethod(beanClass, finderSignature);
    }
}
