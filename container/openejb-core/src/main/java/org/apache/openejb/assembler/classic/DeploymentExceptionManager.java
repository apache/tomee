package org.apache.openejb.assembler.classic;

import org.apache.openejb.assembler.classic.AppInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tomcat swallows some exception when trying to deploy a war.
 * To be able to get it back (from our Deployers for instance)
 * we need a way to store it.
 *
 */
public class DeploymentExceptionManager {
    private static final int MAX_SIZE = Integer.getInteger("tomee.deployement-exception-max-size", 10);
    private final Map<AppInfo, Exception> deploymentException = new LinkedHashMap<AppInfo, Exception>() {
        @Override // just to avoid potential memory leak
        protected boolean removeEldestEntry(Map.Entry<AppInfo, Exception> eldest) {
            return size() > MAX_SIZE;
        }
    };

    public synchronized boolean hasDelpoymentFailed(AppInfo appInfo) {
        return deploymentException.containsKey(appInfo);
    }

    public synchronized Exception getDelpoymentException(AppInfo appInfo) {
        return deploymentException.get(appInfo);
    }

    public synchronized Exception saveDelpoymentException(AppInfo appInfo, Exception exception) {
        return deploymentException.put(appInfo, exception);
    }

    public synchronized void clearDelpoymentException(AppInfo info) {
        deploymentException.remove(info);
    }

    public Exception getFirstException() {
        if (deploymentException.isEmpty()) {
            return null;
        }
        return deploymentException.values().iterator().next();
    }
}
