/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.bean;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

public class BeanParamEntity {
  @CookieParam("bpeCookie")
  public String bpeCookie;

  @FormParam("bpeForm")
  public String bpeForm;

  @HeaderParam("bpeHeader")
  public String bpeHeader;

  @MatrixParam("bpeMatrix")
  public String bpeMatrix;

  @PathParam("bpePath")
  public String bpePath;

  @QueryParam("bpeQuery")
  public String bpeQuery;

  // --------------------
  @BeanParam
  public InnerBeanParamEntity bpeInner;
}
