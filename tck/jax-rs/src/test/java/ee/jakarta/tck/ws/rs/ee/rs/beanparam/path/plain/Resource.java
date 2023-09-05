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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.path.plain;

import ee.jakarta.tck.ws.rs.ee.rs.Constants;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.path.bean.PathBeanParamEntity;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(value = "resource")
public class Resource extends ParamTest {

  @BeanParam
  PathBeanParamEntity field;

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
  @Path("Field/" + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "/{"
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldConstructor() {
    return field();
  }

  @GET
  @Path("Field/" + Constants.PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldFromString() {
    return field();
  }

  @GET
  @Path("Field/" + Constants.PARAM_ENTITY_WITH_VALUEOF + "/{"
      + Constants.PARAM_ENTITY_WITH_VALUEOF + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_VALUEOF + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldValueOf() {
    return field();
  }

  @GET
  @Path("Field/" + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldList() {
    return field();
  }

  @GET
  @Path("Field/" + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldSet() {
    return field();
  }

  @GET
  @Path("Field/" + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING + "}/{"
      + Constants.INNER + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING
      + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldSortedSet() {
    return field();
  }

  @GET
  @Path("Param")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandling(@BeanParam PathBeanParamEntity bean) {
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

  @GET
  @Path("Param/" + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "/{"
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingConstructor(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/" + Constants.PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingFromString(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/" + Constants.PARAM_ENTITY_WITH_VALUEOF + "/{"
      + Constants.PARAM_ENTITY_WITH_VALUEOF + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_VALUEOF + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingValueOf(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/" + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.LIST_PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingList(@BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/" + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.SET_PARAM_ENTITY_WITH_FROMSTRING + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingSet(@BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/" + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING + "/{"
      + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING + "}/{"
      + Constants.INNER + Constants.SORTED_SET_PARAM_ENTITY_WITH_FROMSTRING
      + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingSortedSet(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/1/" + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "/{"
      + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingThrowingByNameFirst(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/2/" + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingThrowingByNameSecond(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/1/" + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "/{"
      + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "}/{"
      + Constants.INNER + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingThrowingWAEFirst(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Param/2/" + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringParamHandlingThrowingWAESecond(
      @BeanParam PathBeanParamEntity bean) {
    return stringParamHandling(bean);
  }

  @GET
  @Path("Field/1/" + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "/{"
      + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "}/{" + Constants.INNER
      + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldThrowingByNameFirst() {
    return field();
  }

  @GET
  @Path("Field/2/" + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.ENTITY_THROWING_EXCEPTION_BY_NAME + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldThrowingByNameSecond() {
    return field();
  }

  @GET
  @Path("Field/1/" + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "/{"
      + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "}/{"
      + Constants.INNER + Constants.PARAM_ENTITY_WITH_CONSTRUCTOR + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldThrowingWAEFirst() {
    return field();
  }

  @GET
  @Path("Field/2/" + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "/{"
      + Constants.PARAM_ENTITY_WITH_FROMSTRING + "}/{" + Constants.INNER
      + Constants.ENTITY_THROWING_WEBAPPLICATIONEXCEPTION + "}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String fieldThrowingWAESecond() {
    return field();
  }
}
