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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.header.bean;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.Constants;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;

public class HeaderBeanParamEntity {
  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.PARAM_ENTITY_WITH_CONSTRUCTOR)
  public ParamEntityWithConstructor paramEntityWithConstructor;

  // @Encoded
  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.PARAM_ENTITY_WITH_FROMSTRING)
  public ParamEntityWithFromString paramEntityWithFromString;

  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.PARAM_ENTITY_WITH_VALUEOF)
  public ParamEntityWithValueOf paramEntityWithValueOf;

  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.SET_PARAM_ENTITY_WITH_FROMSTRING)
  public Set<ParamEntityWithFromString> setParamEntityWithFromString;

  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING)
  public SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString;

  @DefaultValue(Constants.DEFAULT_VALUE)
  @HeaderParam(Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING)
  public List<ParamEntityWithFromString> listParamEntityWithFromString;

  @HeaderParam(Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION)
  public ParamEntityThrowingWebApplicationException entityThrowingWebApplicationException;

  @HeaderParam(Constants.ENTITY_THROWING_EXCEPTION_BY_NAME)
  public ParamEntityThrowingExceptionGivenByName entityThrowingExceptionGivenByName;

  @BeanParam
  public InnerHeaderBeanParamEntity inner;
}
