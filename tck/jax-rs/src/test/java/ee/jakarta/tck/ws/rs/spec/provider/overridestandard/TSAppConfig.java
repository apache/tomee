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

package ee.jakarta.tck.ws.rs.spec.provider.overridestandard;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

public class TSAppConfig extends Application {

  public java.util.Set<java.lang.Class<?>> getClasses() {
    Set<Class<?>> resources = new HashSet<Class<?>>();
    resources.add(Resource.class);
    resources.add(TckByteArrayProvider.class);
    resources.add(TckDataSourceProvider.class);
    resources.add(TckFileProvider.class);
    resources.add(TckInputStreamProvider.class);
    resources.add(TckJaxbProvider.class);
    resources.add(TckMapProvider.class);
    resources.add(TckReaderProvider.class);
    resources.add(TckSourceProvider.class);
    resources.add(TckStreamingOutputProvider.class);
    resources.add(TckStringProvider.class);
    resources.add(TckBooleanProvider.class);
    resources.add(TckCharacterProvider.class);
    resources.add(TckNumberProvider.class);
    return resources;
  }
}
