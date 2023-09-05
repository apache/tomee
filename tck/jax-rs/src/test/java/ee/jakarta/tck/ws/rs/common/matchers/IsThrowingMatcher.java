/*
 * Copyright (c) 2022 Jeremias Weber. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.common.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom Hamcrest Matcher to assert if the given exception is thrown.
 *
 * @author Jeremias Weber
 */
public class IsThrowingMatcher extends TypeSafeMatcher<Runnable> {

    private final Class<? extends Exception> expected;
    private Class<? extends Exception> actual;

    public IsThrowingMatcher(final Class<? extends Exception> expected) {
        this.expected = expected;
    }

    public static Matcher<Runnable> isThrowing(final Class<? extends Exception> expected) {
        return new IsThrowingMatcher(expected);
    }

    @Override
    public boolean matchesSafely(final Runnable action) {
        try {
            action.run();
            return false;
        } catch (Exception e) {
            actual = e.getClass();
            return this.expected.isInstance(e);
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(expected.getName())
                .appendText(" but: was ").appendText(actual.getName());
    }
}
