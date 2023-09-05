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

package ee.jakarta.tck.ws.rs.api.client.clientresponsecontext;

import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;

/**
 * Runtime Delegate for getHeaderStringIsEmptyTest
 */
public class NullStringBeanRuntimeDelegate extends StringBeanRuntimeDelegate {

  public NullStringBeanRuntimeDelegate(RuntimeDelegate orig) {
    super(orig);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0)
      throws IllegalArgumentException {
    if (arg0 == StringBean.class)
      return (HeaderDelegate<T>) new NullStringBeanHeaderDelegate();
    else
      return super.createHeaderDelegate(arg0);
  }

}
