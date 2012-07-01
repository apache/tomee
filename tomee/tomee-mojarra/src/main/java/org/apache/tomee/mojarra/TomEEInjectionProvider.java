package org.apache.tomee.mojarra;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import org.apache.catalina.core.StandardContext;
import org.apache.tomee.catalina.JavaeeInstanceManager;
import org.apache.tomee.catalina.TomEEContainerListener;

public class TomEEInjectionProvider extends DiscoverableInjectionProvider {
    private JavaeeInstanceManager instanceManager;

    public TomEEInjectionProvider() {
        final StandardContext context = TomEEContainerListener.get();
        if (context == null) {
            throw new IllegalArgumentException("standard context cannot be found");
        }
        instanceManager = (JavaeeInstanceManager) context.getInstanceManager();
    }

    @Override
    public void inject(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.inject(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePreDestroy(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.destroyInstance(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePostConstruct(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.postConstruct(managedBean, managedBean.getClass());
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }
}
