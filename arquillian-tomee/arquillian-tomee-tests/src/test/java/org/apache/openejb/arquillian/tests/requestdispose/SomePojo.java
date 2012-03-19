package org.apache.openejb.arquillian.tests.requestdispose;

public class SomePojo implements Pojo {
    public SomePojo(final String justHereToAvoidToBeManaged) {
        // no-op
    }

    @Override
    public void foo() {
        // no-op
    }
}
