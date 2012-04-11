package org.apache.openejb.itest.tomee;

import java.io.File;

public interface ServerTweaker {
    void tweak(File home);
}
