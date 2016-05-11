/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package test.patch;

import test.patch.foo.Another;

public final class MyMain {
    private MyMain() {
        // no-op
    }

    public static int test() {
        return Another.val();
    }
}
