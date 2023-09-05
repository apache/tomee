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

package ee.jakarta.tck.ws.rs.api.client.clientresponsecontext;

import jakarta.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;

/**
 * Header Delegate for getHeaderStringIsEmptyTest
 */
public class NullStringBeanHeaderDelegate
    implements HeaderDelegate<StringBean> {

  @Override
  public StringBean fromString(String arg0) throws IllegalArgumentException {
    return new StringBean(arg0);
  }

  @Override
  public String toString(StringBean arg0) throws IllegalArgumentException {
    // By design. We need to set header as a no value
    // so that then the ClientResponseContext.getHeaderString should return
    // an empty string
    return null;
  }

}
