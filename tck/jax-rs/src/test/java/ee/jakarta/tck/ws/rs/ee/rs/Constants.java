/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs;

/**
 * The constants to be used on server resources for
 * any @PathParam, @MatrixParam, ... and any other @Param
 * 
 * @since 2.0.1
 */
public interface Constants {
  public static final String INNER = "Inner";

  public static final String DEFAULT_VALUE = "DefaultParamValue";

  public static final String PARAM_ENTITY_WITH_CONSTRUCTOR = "ParamEntityWithConstructor";

  public static final String PARAM_ENTITY_WITH_FROMSTRING = "ParamEntityWithFromString";

  public static final String PARAM_ENTITY_WITH_VALUEOF = "ParamEntityWithValueOf";

  public static final String SET_PARAM_ENTITY_WITH_FROMSTRING = "SetParamEntityWithFromString";

  public static final String SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING = "SortedSetParamEntityWithFromString";

  public static final String LIST_PARAM_ENTITY_WITH_FROMSTRING = "ListParamEntityWithFromString";

  public static final String ENTITY_THROWING_WEBAPPLICATIONEXCEPTION = "ParamEntityThrowingWebApplicationException";

  public static final String ENTITY_THROWING_EXCEPTION_BY_NAME = "ParamEntityThrowingExceptionGivenByName";
}
