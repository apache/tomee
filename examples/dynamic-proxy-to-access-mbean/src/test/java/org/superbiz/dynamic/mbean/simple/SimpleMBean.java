package org.superbiz.dynamic.mbean.simple;

/**
 * @author rmannibucau
 */
public interface SimpleMBean {
    int length(String s);

    int getCounter();
    void setCounter(int c);
}
