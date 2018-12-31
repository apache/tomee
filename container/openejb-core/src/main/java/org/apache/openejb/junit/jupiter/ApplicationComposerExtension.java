package org.apache.openejb.junit.jupiter;

import org.apache.openejb.testing.ApplicationComposers;


import org.apache.openejb.testing.ApplicationComposers;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApplicationComposerExtension implements BeforeEachCallback, AfterEachCallback {

    private ApplicationComposers delegate;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        delegate = new ApplicationComposers(context.getRequiredTestInstance());
        delegate.before(context.getRequiredTestInstance());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        delegate.after();
    }

}
