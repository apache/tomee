package org.apache.openejb.concurrencyutilities.ee.factory;

import org.apache.openejb.concurrencyutilities.ee.impl.ManagedThreadFactoryImpl;

import javax.enterprise.concurrent.ManagedThreadFactory;

public class ManagedThreadFactoryImplFactory {
    private String prefix = "openejb-managed-thread-";

    public ManagedThreadFactory create() {
        return new ManagedThreadFactoryImpl(prefix);
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
}
