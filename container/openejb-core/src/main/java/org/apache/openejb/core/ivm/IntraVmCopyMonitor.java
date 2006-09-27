package org.apache.openejb.core.ivm;

import org.apache.openejb.util.FastThreadLocal;

public class IntraVmCopyMonitor {

    private static FastThreadLocal threadStorage = new FastThreadLocal();

    boolean intraVmCopyOperation = false;

    boolean statefulPassivationOperation = false;

    IntraVmCopyMonitor() {
    }

    public static boolean exists() {
        return (threadStorage.get() != null);
    }

    public static void release() {
        threadStorage.set(null);
    }

    static IntraVmCopyMonitor getMonitor() {
        IntraVmCopyMonitor monitor = (IntraVmCopyMonitor) threadStorage.get();
        if (monitor == null) {
            monitor = new IntraVmCopyMonitor();
            threadStorage.set(monitor);
        }
        return monitor;
    }

    public static void preCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = true;
    }

    public static void postCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = false;
    }

    public static void prePassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = true;
    }

    public static void postPassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = false;
    }

    public static boolean isIntraVmCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        if (monitor.intraVmCopyOperation)
            return true;
        else
            return false;
    }

    public static boolean isStatefulPassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        if (monitor.statefulPassivationOperation)
            return true;
        else
            return false;
    }
}
