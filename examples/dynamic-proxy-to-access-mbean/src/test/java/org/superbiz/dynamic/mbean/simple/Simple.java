package org.superbiz.dynamic.mbean.simple;

/**
 * @author rmannibucau
 */
public class Simple implements SimpleMBean {
    private int counter = 0;

    @Override public int length(String s) {
        if (s == null) {
            return 0;
        }
        return s.length();
    }

    @Override public int getCounter() {
        return counter;
    }

    @Override public void setCounter(int c) {
        counter = c;
    }
}
