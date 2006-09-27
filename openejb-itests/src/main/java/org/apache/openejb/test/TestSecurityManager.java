package org.apache.openejb.test;

import java.security.Permission;

/**
 * This class is temporary
 */
public class TestSecurityManager extends SecurityManager {

    /**
     * Constructs a new <code>TestSecurityManager</code>.
     */
    public TestSecurityManager() {
    }

    public void checkPermission(Permission perm) {
    }
    
    public void checkPermission(Permission perm, Object context) {
    }

}
