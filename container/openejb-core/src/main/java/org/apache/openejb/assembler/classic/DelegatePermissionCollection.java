package org.apache.openejb.assembler.classic;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.ArrayEnumeration;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelegatePermissionCollection extends PermissionCollection {
    private static final String PERMISSION_COLLECTION_CLASS = "openejb.permission-collection.class";

    private PermissionCollection pc = getPermissionCollection();

    @Override
    public void add(Permission permission) {
        pc.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
        return pc.implies(permission);
    }

    @Override
    public Enumeration<Permission> elements() {
        return pc.elements();
    }

    public static PermissionCollection getPermissionCollection() {
        try {
            return (PermissionCollection) DelegatePermissionCollection.class.getClassLoader()
                    .loadClass(
                            SystemInstance.get().getOptions().get(PERMISSION_COLLECTION_CLASS,
                                    FastPermissionCollection.class.getName()))
                    .newInstance();
        } catch (Exception cnfe) {
            // return new Permissions(); // the jdk implementation, it seems slow at least for startup up
            return new FastPermissionCollection();
        }
    }

    public static class FastPermissionCollection extends PermissionCollection {
        private static final int MAX_CACHE_SIZE = SystemInstance.get().getOptions().get("openejb.permission-collection.cache.size", 3000);
        private final List<Permission> permissions = new ArrayList<Permission>();
        private final Map<Permission, Boolean> alreadyEvaluatedPermissions = new ConcurrentHashMap<Permission, Boolean>();

        @Override
        public synchronized void add(Permission permission) {
            permissions.add(permission);
        }

        @Override
        public synchronized boolean implies(Permission permission) {
            if (alreadyEvaluatedPermissions.containsKey(permission)) {
                return alreadyEvaluatedPermissions.get(permission);
            }

            // clear the cache if it is too big
            // TODO: look if we should use a FIFO strategy or sthg like that
            if (alreadyEvaluatedPermissions.size() > MAX_CACHE_SIZE) {
                alreadyEvaluatedPermissions.clear();
            }

            for (Permission perm : permissions) {
                if (perm.implies(permission)) {
                    alreadyEvaluatedPermissions.put(permission, true);
                    return true;
                }
            }
            alreadyEvaluatedPermissions.put(permission, false);
            return false;
        }

        @Override
        public synchronized Enumeration<Permission> elements() {
            return new ArrayEnumeration(permissions);
        }
    }
}
