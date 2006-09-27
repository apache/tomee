package org.apache.openejb.util;

import java.util.ArrayList;
import java.util.EmptyStackException;

public final class ArrayStack extends ArrayList implements Stack {

    public void setSize(int newSize) {
        if (newSize == 0) clear();
        else {
            for (int i = size() - 1; i >= newSize; i--)
                remove(i);
        }
    }

    public Object push(Object item) {
        add(item);
        return item;
    }

    public Object pop() {
        Object obj;
        obj = peek();

        int len = size();
        remove(len - 1);

        return obj;
    }

    public Object peek() {
        int len = size();

        if (len == 0) throw new EmptyStackException();

        return get(len - 1);
    }
}
