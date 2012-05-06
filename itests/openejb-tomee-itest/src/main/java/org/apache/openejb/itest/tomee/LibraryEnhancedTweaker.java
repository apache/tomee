package org.apache.openejb.itest.tomee;

import java.io.File;

public class LibraryEnhancedTweaker extends SimpleTweaker {
    private final ServerTweaker enhanced;
    private final File lib;

    public LibraryEnhancedTweaker(final ServerTweaker tweaker, final File lib) {
        this.enhanced = tweaker;
        this.lib = lib;
    }

    @Override
    public void tweak(final File home) {
        enhanced.tweak(home);
        addLib(home, lib);
    }
}
