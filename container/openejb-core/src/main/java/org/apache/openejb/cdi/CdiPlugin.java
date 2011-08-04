/*
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
package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.ee.event.TransactionalEventNotifier;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.util.WebBeansUtil;

import javax.ejb.Stateful;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

public class CdiPlugin extends AbstractOwbPlugin implements OpenWebBeansJavaEEPlugin, OpenWebBeansEjbPlugin {

	private AppContext appContext;
	private Set<Class<?>> beans;

	private WebBeansContext webBeansContext;
	private CdiAppContextsService contexsServices;

	@Override
	public void shutDown() {
		super.shutDown();
		// this plugin may have been installed in a non-ejb lifecycle???
		if (beans != null) {
			this.beans.clear();
		}
	}

	public void setAppContext(AppContext appContext) {
		this.appContext = appContext;
	}

	public void configureDeployments(List<BeanContext> ejbDeployments) {
		WeakHashMap<Class<?>, Object> beans = new WeakHashMap<Class<?>, Object>();
		for (BeanContext deployment : ejbDeployments) {
			if (deployment.getComponentType().isSession()) {
				beans.put(deployment.getBeanClass(), null);
			}
		}
		this.beans = beans.keySet();
	}

	public CdiAppContextsService getContexsServices() {
		return contexsServices;
	}

	public void startup() {
		webBeansContext = appContext.getWebBeansContext();
		this.contexsServices = (CdiAppContextsService) webBeansContext.getContextsService();
		this.contexsServices.init(null);
	}

	public void stop() throws OpenEJBException {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		try {
			// Setting context class loader for cleaning
			Thread.currentThread().setContextClassLoader(appContext.getClassLoader());

			// Fire shut down
			appContext.getBeanManager().fireEvent(new BeforeShutdownImpl(), new Annotation[0]);

			// Destroys context
			this.contexsServices.destroy(null);

			// Free all plugin resources
            webBeansContext.getPluginLoader().shutDown();

			// Clear extensions
			webBeansContext.getExtensionLoader().clear();

			// Delete Resolutions Cache
			InjectionResolver.getInstance().clearCaches();

			// Delte proxies
			webBeansContext.getJavassistProxyFactory().clear();

			// Delete AnnotateTypeCache
			webBeansContext.getAnnotatedElementFactory().clear();

			// JMs Manager clear
			webBeansContext.getjMSManager().clear();

			// Clear the resource injection service
			CdiResourceInjectionService injectionServices = (CdiResourceInjectionService) webBeansContext.getService(ResourceInjectionService.class);
			injectionServices.clear();

			// Clear singleton list
			WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

		} catch (Exception e) {
			throw new OpenEJBException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
	}

	@Override
	public <T> T getSupportedService(Class<T> serviceClass) {
		return supportService(serviceClass) ? serviceClass.cast(this) : null;
	}

	@Override
	public void isManagedBean(Class<?> clazz) {
	}

	@Override
	public boolean supportService(Class<?> serviceClass) {
		return serviceClass == TransactionService.class || serviceClass == SecurityService.class;
	}

	@Override
 	public Object getSessionBeanProxy(Bean<?> bean, Class<?> interfce, CreationalContext<?> creationalContext) {

        final Context context = webBeansContext.getBeanManagerImpl().getContext(bean.getScope());

        final CreationalContext<Object> cc = (CreationalContext<Object>) creationalContext;
        final Contextual<Object> component = (Contextual<Object>) bean;

        return context.get(component, cc);

	}

	@Override
	public boolean isSessionBean(Class<?> clazz) {
		// this may be called from a web app without ejbs in which case beans will not have been initialized by openejb.
		return beans != null && beans.contains(clazz);
	}

	@Override
	public <T> Bean<T> defineSessionBean(Class<T> clazz, ProcessAnnotatedType<T> processAnnotateTypeEvent) {
		throw new IllegalStateException("Statement should never be reached");
	}

	@Override
	public boolean isSingletonBean(Class<?> clazz) {
		throw new IllegalStateException("Statement should never be reached");
	}

	@Override
	public boolean isStatefulBean(Class<?> clazz) {
        // TODO Make the EjbPlugin pass in the Bean<T> instance
		return clazz.isAnnotationPresent(Stateful.class);
	}

	@Override
	public boolean isStatelessBean(Class<?> clazz) {
		throw new IllegalStateException("Statement should never be reached");
	}

    @Override
    public Method resolveViewMethod(Bean<?> component, Method declaredMethod) {
//        if (true)return declaredMethod;
        if (!(component instanceof CdiEjbBean)) return declaredMethod;

        CdiEjbBean cdiEjbBean = (CdiEjbBean) component;

        final BeanContext beanContext = cdiEjbBean.getBeanContext();

        for (Class intface : beanContext.getBusinessLocalInterfaces()) {
            try {
                return intface.getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
            } catch (NoSuchMethodException e) {
            }
        }
        return declaredMethod;
    }

    //TODO Delete if we end up not needing this
    public Method resolveBeanMethod(Bean<?> component, Method declaredMethod) {
        if (!(component instanceof CdiEjbBean)) return declaredMethod;

        CdiEjbBean cdiEjbBean = (CdiEjbBean) component;

        final BeanContext beanContext = cdiEjbBean.getBeanContext();

        try {
            return beanContext.getBeanClass().getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return declaredMethod;
        }
    }
}
