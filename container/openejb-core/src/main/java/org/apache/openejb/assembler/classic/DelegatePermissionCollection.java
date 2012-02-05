package org.apache.openejb.assembler.classic;

import org.apache.openejb.util.ArrayEnumeration;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
                            System.getProperty(PERMISSION_COLLECTION_CLASS,
                                    Permissions.class.getName()))
                    .newInstance();
        } catch (Exception cnfe) {
            return new Permissions();
        }
    }

    public static class FastPermissionCollection extends PermissionCollection {
        private final List<Permission> permissions = new ArrayList<Permission>();

        @Override
        public synchronized void add(Permission permission) {
            permissions.add(permission);
        }

        @Override
        public synchronized boolean implies(Permission permission) {
            for (Permission perm : permissions) {
                if (perm.implies(perm)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public synchronized Enumeration<Permission> elements() {
            return new ArrayEnumeration(permissions);
        }
    }
}
