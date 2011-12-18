package org.superbiz.dynamic.mbean.simple;

public interface SimpleMBean {
    int length(String s);

    int getCounter();
    void setCounter(int c);
}
