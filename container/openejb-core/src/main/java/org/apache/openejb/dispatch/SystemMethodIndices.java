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
package org.apache.openejb.dispatch;

import javax.ejb.Timer;

import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EjbInvocationImpl;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.timer.EJBTimeoutInvocationFactory;

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
