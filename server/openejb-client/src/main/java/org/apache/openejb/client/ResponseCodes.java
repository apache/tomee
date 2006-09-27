package org.apache.openejb.client;

public interface ResponseCodes {

    public static final int AUTH_GRANTED = 1;
    public static final int AUTH_REDIRECT = 2;
    public static final int AUTH_DENIED = 3;
    public static final int EJB_OK = 4;
    public static final int EJB_OK_CREATE = 5;
    public static final int EJB_OK_FOUND = 6;
    public static final int EJB_OK_FOUND_COLLECTION = 7;
    public static final int EJB_OK_NOT_FOUND = 8;
    public static final int EJB_APP_EXCEPTION = 9;
    public static final int EJB_SYS_EXCEPTION = 10;
    public static final int EJB_ERROR = 11;
    public static final int JNDI_OK = 12;
    public static final int JNDI_EJBHOME = 13;
    public static final int JNDI_CONTEXT = 14;
    public static final int JNDI_ENUMERATION = 15;
    public static final int JNDI_NOT_FOUND = 16;
    public static final int JNDI_NAMING_EXCEPTION = 17;
    public static final int JNDI_RUNTIME_EXCEPTION = 18;
    public static final int JNDI_ERROR = 19;
    public static final int EJB_OK_FOUND_ENUMERATION = 20;
}

