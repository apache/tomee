/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.configurable;

import jakarta.ws.rs.core.Configurable;

/**
 * We need the possibility to override the way the registerable is registered
 * The default behavior is for single argument register method
 */
public class Registrar {
    public void register(Configurable<?> config, Object registerable) {
        if (registerable instanceof Class) // register(Class)
            config.register((Class<?>) registerable);
        else if (registerable instanceof String) // setProperty()
            config.property((String) registerable, registerable);
        else
            // register(Object)
            config.register(registerable);
    }
}
