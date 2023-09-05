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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.query.plain;

import ee.jakarta.tck.ws.rs.ee.rs.beanparam.query.bean.QueryBeanParamEntity;
import ee.jakarta.tck.ws.rs.ee.rs.Constants;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(value = "resource")
public class Resource extends ParamTest {

  @BeanParam
  QueryBeanParamEntity field;

  @GET
  @Path("Field")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String field() {
    sb = new StringBuilder();
    setReturnValues(field.paramEntityWithConstructor,
        field.paramEntityWithFromString, field.paramEntityWithValueOf,
        field.setParamEntityWithFromString,
        field.sortedSetParamEntityWithFromString,
        field.listParamEntityWithFromString, FIELD);
    setReturnValues(field.inner.paramEntityWithConstructor,
        field.inner.paramEntityWithFromString,
        field.inner.paramEntityWithValueOf,
        field.inner.setParamEntityWithFromString,
        field.inner.sortedSetParamEntityWithFromString,
        field.inner.listParamEntityWithFromString, Constants.INNER + FIELD);
    return sb.toString();
  }

  @GET
  @Path("Param")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandling(@BeanParam QueryBeanParamEntity bean) {
    sb = new StringBuilder();
    setReturnValues(bean.paramEntityWithConstructor,
        bean.paramEntityWithFromString, bean.paramEntityWithValueOf,
        bean.setParamEntityWithFromString,
        bean.sortedSetParamEntityWithFromString,
        bean.listParamEntityWithFromString, PARAM);
    setReturnValues(bean.inner.paramEntityWithConstructor,
        bean.inner.paramEntityWithFromString, bean.inner.paramEntityWithValueOf,
        bean.inner.setParamEntityWithFromString,
        bean.inner.sortedSetParamEntityWithFromString,
        bean.inner.listParamEntityWithFromString, Constants.INNER + PARAM);
    return sb.toString();
  }
}
