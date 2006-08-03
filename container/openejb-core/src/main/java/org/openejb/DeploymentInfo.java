package org.openejb;

import java.lang.reflect.Method;

public interface DeploymentInfo {

    final public static byte TX_NEVER = (byte) 0;

    final public static byte TX_NOT_SUPPORTED = (byte) 1;

    final public static byte TX_SUPPORTS = (byte) 2;

    final public static byte TX_MANDITORY = (byte) 3;

    final public static byte TX_REQUIRED = (byte) 4;

    final public static byte TX_REQUIRES_NEW = (byte) 5;

    final public static byte STATEFUL = (byte) 6;

    final public static byte STATELESS = (byte) 7;

    final public static byte BMP_ENTITY = (byte) 8;

    final public static byte CMP_ENTITY = (byte) 9;

    final public static byte MESSAGE_DRIVEN = (byte) 10;

    final public static String AC_CREATE_EJBHOME = "create.ejbhome";

    public byte getComponentType();

    public byte getTransactionAttribute(Method method);

    public String [] getAuthorizedRoles(Method method);

    public String [] getAuthorizedRoles(String action);

    public Container getContainer();

    public Object getDeploymentID();

    public boolean isBeanManagedTransaction();

    public Class getHomeInterface();

    public Class getLocalHomeInterface();

    public Class getLocalInterface();

    public Class getRemoteInterface();

    public Class getBeanClass();

    public Class getPrimaryKeyClass();

    public java.lang.reflect.Field getPrimaryKeyField();

    public boolean isReentrant();

    public interface BusinessLocalHome extends javax.ejb.EJBLocalHome {
        Object create();
    }
    public interface BusinessRemoteHome extends javax.ejb.EJBHome {
        Object create();
    }
}
