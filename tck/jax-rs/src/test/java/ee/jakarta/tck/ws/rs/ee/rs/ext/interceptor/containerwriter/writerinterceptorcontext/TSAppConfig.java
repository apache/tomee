/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter.writerinterceptorcontext;

import java.util.HashSet;
import java.util.Set;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.InputStreamReaderProvider;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.ProceedExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.WriterInterceptorOne;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.WriterInterceptorTwo;

import jakarta.ws.rs.core.Application;

public class TSAppConfig extends Application {

  public java.util.Set<java.lang.Class<?>> getClasses() {
    Set<Class<?>> resources = new HashSet<Class<?>>();
    resources.add(Resource.class);
    resources.add(WriterInterceptorOne.class);
    resources.add(WriterInterceptorTwo.class);
    resources.add(InputStreamReaderProvider.class);
    resources.add(StringBeanEntityProvider.class);
    resources.add(ProceedExceptionMapper.class);
    return resources;
  }
}
