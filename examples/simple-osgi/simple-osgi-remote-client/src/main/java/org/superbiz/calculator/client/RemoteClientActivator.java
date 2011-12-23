package org.superbiz.calculator.client;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RemoteClientActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        // set the bundle classloader to avoid to go back to AppClassloader and not found imported classes
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            ClientUtil.invoke();
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // no-op
    }
}
