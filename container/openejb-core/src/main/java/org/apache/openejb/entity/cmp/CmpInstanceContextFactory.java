/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
