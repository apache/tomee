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

package ee.jakarta.tck.ws.rs.spec.provider.exceptionmapper;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

public class TSAppConfig extends Application {

  public java.util.Set<java.lang.Class<?>> getClasses() {
    Set<Class<?>> resources = new HashSet<Class<?>>();
    resources.add(Resource.class);
    resources.add(ThrowableMapper.class);
    resources.add(PlainExceptionMapper.class);
    resources.add(RuntimeExceptionMapper.class);
    resources.add(WebAppExceptionMapper.class);
    resources.add(ClientErrorExceptionMapper.class);
    // --for JAXRS:SPEC:82, but affects all responses
    resources.add(FilterChainTestExceptionMapper.class);
    resources.add(ResponseFilter.class);
    // --for JAXRS:SPEC:83
    resources.add(ExceptionFromMappedExceptionMapper.class);
    return resources;
  }
}
