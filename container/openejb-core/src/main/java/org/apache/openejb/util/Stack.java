package org.apache.openejb.util;

public interface Stack {
    public Object pop() throws java.util.EmptyStackException;

    public Object push(Object obj);

    public int size();
}