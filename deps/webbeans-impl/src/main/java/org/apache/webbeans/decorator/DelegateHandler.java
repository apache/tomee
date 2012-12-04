/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.decorator;

import org.apache.webbeans.component.EnterpriseBeanMarker;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.proxy.MethodHandler;
import org.apache.webbeans.util.WebBeansUtil;

import javax.interceptor.InvocationContext;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DelegateHandler implements InvocationHandler, MethodHandler, Serializable, Externalizable
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(DelegateHandler.class);
    private static final long serialVersionUID = -3063755008944970684L;

    private transient List<Object> decorators;
    public static transient ThreadLocal<AtomicInteger> position = new ThreadLocal<AtomicInteger>()
    {
        @Override
        protected AtomicInteger initialValue()
        {
            return new AtomicInteger(0);
        }
    };

    private transient Object actualInstance = null;
    
    private transient OwbBean<?> bean = null;
    
    private transient InvocationContext ejbContext;

    //Do not remove this constructor, used by passivation.
    public DelegateHandler()
    {
        // no-op
    }

    public DelegateHandler(OwbBean<?> bean)
    {
        this.bean = bean;
    }
    
    public DelegateHandler(OwbBean<?> bean, InvocationContext ejbContext)
    {
        this.bean = bean;
        this.ejbContext = ejbContext;
    }    
    
    public Object invoke(Object instance, Method method, Method proceed, Object[] arguments) throws Exception
    {
        return invoke(instance, method, arguments);
    }

    public Object invoke(Object instance, Method method, Object[] arguments) throws Exception
    {
        // Tuck away a reference to the bean being Decorated
        if (actualInstance == null)
        {
            actualInstance = instance;
        }

        int hit = 0;
        int decoratorsSize = decorators.size();
        while (position.get().intValue() < decoratorsSize)
        {
            hit++;

            int currentPosition = position.get().intValue();
            Object decorator = decorators.get(position.get().getAndIncrement());
            try
            {
                Method decMethod = decorator.getClass().getMethod(method.getName(), method.getParameterTypes());
                boolean methodInInterface = checkForMethodInInterfaces(decorator.getClass(), method);

                if (decMethod != null && methodInInterface)
                {
                    if (!decMethod.isAccessible())
                    {
                        bean.getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(decMethod, true);
                    }

                    try
                    {
                        return decMethod.invoke(decorator, arguments);
                    }
                    finally
                    {
                        if (currentPosition == 0) // if we go back on the first decorator no more need of this thread local
                        {
                            position.remove();
                        }
                    }
                }

            }
            catch (SecurityException e)
            {
                logger.log(Level.SEVERE, OWBLogConst.ERROR_0011, WebBeansLoggerFacade.args(method.getName(), decorator.getClass().getName()));
                throw new WebBeansException(e);

            }
            catch (NoSuchMethodException e)
            {
                continue;
            }
            catch (InvocationTargetException e)
            {
                Throwable cause = e.getCause();
                //If the wrapped exception tells us the method didn't exist, continue
                if(cause instanceof NoSuchMethodException)
                {
                    continue;
                }

                logger.log(Level.SEVERE, OWBLogConst.ERROR_0012, WebBeansLoggerFacade.args(e.getTargetException(), method.getName(), decorator.getClass().getName()));

                if (cause instanceof Exception)
                {
                    throw (Exception) cause;
                }
                else if (cause instanceof Error)
                {
                    throw (Error) cause;
                }
                else
                {
                    throw new WebBeansException(e);
                }
            }
            catch (IllegalAccessException e)
            {
                logger.log(Level.SEVERE, OWBLogConst.ERROR_0014, WebBeansLoggerFacade.args(method.getName(), decorator.getClass().getName()));
                throw new WebBeansException(e);
            }

        }

        if (hit == decoratorsSize) // if we hit all decorators but noone was called (see while loop) then clean here
        {
            position.remove();
        }

        if (!method.isAccessible())
        {
            bean.getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(method, true);
        }

        Object result = null;
        
        if(!(bean instanceof EnterpriseBeanMarker))
        {
            result = method.invoke(actualInstance, arguments);
        }
        else
        {
            if(ejbContext != null)
            {
                Method ejbMethod = ejbContext.getMethod();
                
                // don't use method.equals(), it may only differ by being abstract in the EJB proxy.
                 
                if (method.getName().equals(ejbMethod.getName()) && 
                        method.getReturnType().equals(ejbMethod.getReturnType())) 
                {
                    result = ejbContext.proceed();
                }

                else 
                {
                    Object ejbInstance = ejbContext.getTarget();
                    result = method.invoke(ejbInstance, arguments);
                }
            }
        }

        return result;

    }

    /**
     * Helper method to locate method in any of the interfaces of the Decorator.
     * 
     * @param class1 whose interfaces we want to check
     * @param m to check for in Interfaces
     * @return True if the method exists in any of the interfaces of the
     *         Decorator
     */
    private boolean checkForMethodInInterfaces(Class<?> class1, Method m)
    {
        Class<?>[] interfaces = class1.getInterfaces();
        for (int i = 0; i < interfaces.length; i++)
        {
            try
            {
                if (interfaces[i].getMethod(m.getName(), m.getParameterTypes()) != null)
                {
                    return true;
                }
            }
            catch (NoSuchMethodException exception)
            {
                // no-op, we didn't find the method
            }
        }
        //
        return false;
    }

    public void setDecorators(List<Object> dec)
    {
        decorators = dec;
    }

    public void writeExternal(ObjectOutput out) throws IOException 
    {
        String id = WebBeansUtil.isPassivationCapable(bean);
        if (id != null) 
        {
            out.writeObject(id);
            out.writeObject(decorators);
        }
        else 
        {
            out.writeObject("");
        }
        //TODO: ejbContext is not serializable, need to re-grab it
        //from the env.
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException 
    {
        String id = (String)in.readObject();
        if (id.equals("")) 
        {
            return;
        }
        bean = (OwbBean<?>) WebBeansContext.currentInstance().getBeanManagerImpl().getPassivationCapableBean(id);
        decorators = (List<Object>) in.readObject();
    }
}
