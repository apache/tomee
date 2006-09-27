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
package org.apache.openejb.entity.cmp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import org.apache.openejb.CmpEjbContainer;
import org.apache.openejb.CmpEjbDeployment;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.dispatch.MethodHelper;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.proxy.EJBProxyFactory;

/**
 * @version $Revision$ $Date$
 */
public class CmpInstanceContextFactory implements InstanceContextFactory {
    private final CmpEjbDeployment cmpEjbDeployment;
    private final CmpEjbContainer cmpEjbContainer;
    private final EJBProxyFactory proxyFactory;
    private final boolean cmp2;

    private final InstanceOperation[] itable;
    private final Enhancer enhancer;
    private final FastClass beanFastClass;

    public CmpInstanceContextFactory(CmpEjbDeployment cmpEjbDeployment,
            CmpEjbContainer cmpEjbContainer,
            EJBProxyFactory proxyFactory,
            boolean cmp2,
            Map imap) {
        this.cmpEjbContainer = cmpEjbContainer;
        this.proxyFactory = proxyFactory;
        this.cmpEjbDeployment = cmpEjbDeployment;
        this.cmp2 = cmp2;

        Class beanClass = cmpEjbDeployment.getBeanClass();
        if (cmp2) {
            // create a factory to generate concrete subclasses of the abstract cmp implementation class
            enhancer = new Enhancer();
            enhancer.setSuperclass(beanClass);
            enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
            enhancer.setCallbackFilter(FILTER);
            enhancer.setUseFactory(false);
            Class enhancedClass = enhancer.createClass();

            beanFastClass = FastClass.create(enhancedClass);

            itable = new InstanceOperation[beanFastClass.getMaxIndex() + 1];
            for (Iterator iterator = imap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                MethodSignature signature = (MethodSignature) entry.getKey();
                InstanceOperation iop = (InstanceOperation) entry.getValue();
                int index = MethodHelper.getSuperIndex(enhancedClass, signature);
                if (index < 0) {
                    throw new IllegalStateException("Based on the EJB configuration I expected to find a method " +
                            beanClass.getName() + "." + signature.toString() + " but no such method was found");
                }
                itable[index] = iop;
            }
        } else {
            enhancer = null;
            itable = null;
            beanFastClass = FastClass.create(beanClass);
        }
    }

    public synchronized EJBInstanceContext newInstance() throws Exception {
        CmpMethodInterceptor cmpMethodInterceptor = new CmpMethodInterceptor(itable);
        EntityBean instance = createCMPBeanInstance(cmpMethodInterceptor);
        CmpInstanceContext context = new CmpInstanceContext(cmpEjbDeployment,
                cmpEjbContainer,
                instance,
                proxyFactory
        );
        cmpMethodInterceptor.setInstanceContext(context);
        return context;
    }

    private EntityBean createCMPBeanInstance(CmpMethodInterceptor cmpMethodInterceptor) {
        if (cmp2) {
            synchronized (this) {
                enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, cmpMethodInterceptor});
                return (EntityBean) enhancer.create();
            }
        } else {
            try {
                return (EntityBean) beanFastClass.newInstance();
            } catch (InvocationTargetException e) {
                throw new EJBException("Unable to create entity bean instance", e);
            }
        }
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            if (Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }
            return 0;
        }
    };

}
