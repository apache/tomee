package org.apache.tomee.catalina;

import org.apache.openejb.assembler.classic.AppInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tomcat swallows some exception when trying to deploy a war.
 * To be able to get it back (from our Deployers for instance)
 * we need a way to store it.
 *
 * @author rmannibucau
 */
public class DeploymentExceptionManager {
    private static final int MAX_SIZE = Integer.getInteger("tomee.deployement-exception-max-size", 10);
    private final Map<AppInfo, Exception> deploymentException = new LinkedHashMap<AppInfo, Exception>() {
        @Override // just to avoid potential memory leak
        protected boolean removeEldestEntry(Map.Entry<AppInfo, Exception> eldest) {
            return size() > MAX_SIZE;
        }
    };

    public synchronized boolean hasDelpoyementFailed(AppInfo appInfo) {
        return deploymentException.containsKey(appInfo);
    }

    public synchronized Exception getDelpoyementException(AppInfo appInfo) {
        return deploymentException.get(appInfo);
    }

    public synchronized Exception saveDelpoyementException(AppInfo appInfo, Exception exception) {
        return deploymentException.put(appInfo, exception);
    }

    public synchronized void clearDelpoyementException(AppInfo info) {
        deploymentException.remove(info);
    }

    public Exception getFirstException() {
        if (deploymentException.isEmpty()) {
            return null;
        }
        return deploymentException.values().iterator().next();
    }
}
