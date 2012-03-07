package org.apache.openejb.mbeans;

import javax.management.Description;
import javax.management.ManagedAttribute;

// simply a copy of ReaderWriter but abstract to avoid CDI conflict
public abstract class Parent {
    private int i = -1;

    @ManagedAttribute
    @Description("just a value") public int getValue() {
        if (i < 0) {
            return 2;
        }
        return i;
    }

    @ManagedAttribute public void setValue(int v) {
        i = v;
    }
}
