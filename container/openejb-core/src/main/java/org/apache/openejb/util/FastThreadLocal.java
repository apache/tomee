package org.apache.openejb.util;

// This class used to be a home grown implementation, which had thread
// safety issues, and created one thread per instance. Using ThreadLocal
// doesn't bring any performance penalty in a JDK 1.3 vm. See OpenEJB issue 505128 for details

public class FastThreadLocal extends java.lang.ThreadLocal {
}
