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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.plain;

import ee.jakarta.tck.ws.rs.ee.rs.beanparam.bean.BeanParamEntity;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("resource")
public class Resource {

  @BeanParam
  BeanParamEntity fieldBeanParam;

  @POST
  @Path("queryparam")
  public String queryParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpeQuery, bean.bpeInner.innerQuery);
  }

  @POST
  @Path("queryfield")
  public String queryField(String content) {
    return appnd(content, fieldBeanParam.bpeQuery,
        fieldBeanParam.bpeInner.innerQuery);
  }

  @POST
  @Path("formparam")
  public String formParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpeForm, bean.bpeInner.innerForm);
  }

  @POST
  @Path("formfield")
  public String formField(String content) {
    return appnd(content, fieldBeanParam.bpeForm,
        fieldBeanParam.bpeInner.innerForm);
  }

  @POST
  @Path("headerparam")
  public String headerParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpeHeader, bean.bpeInner.innerHeader);
  }

  @POST
  @Path("headerfield")
  public String headerField(String content) {
    return appnd(content, fieldBeanParam.bpeHeader,
        fieldBeanParam.bpeInner.innerHeader);
  }

  @POST
  @Path("pathparam/{bpePath}/{innerPath}")
  public String pathParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpePath, bean.bpeInner.innerPath);
  }

  @POST
  @Path("pathfield/{bpePath}/{innerPath}")
  public String pathField(String content) {
    return appnd(content, fieldBeanParam.bpePath,
        fieldBeanParam.bpeInner.innerPath);
  }

  @POST
  @Path("matrixparam")
  public String matrixParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpeMatrix, bean.bpeInner.innerMatrix);
  }

  @POST
  @Path("matrixfield")
  public String matrixField(String content) {
    return appnd(content, fieldBeanParam.bpeMatrix,
        fieldBeanParam.bpeInner.innerMatrix);
  }

  @POST
  @Path("cookieparam")
  public String cookieParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(content, bean.bpeCookie, bean.bpeInner.innerCookie);
  }

  @POST
  @Path("cookiefield")
  public String cookieField(String content) {
    return appnd(content, fieldBeanParam.bpeCookie,
        fieldBeanParam.bpeInner.innerCookie);
  }

  @POST
  @Path("allfield/{bpePath}/{innerPath}")
  public String everyField(String content) {
    return appnd(fieldBeanParam.bpeForm //
        , fieldBeanParam.bpeInner.innerForm //
        , fieldBeanParam.bpeHeader //
        , fieldBeanParam.bpeInner.innerHeader //
        , fieldBeanParam.bpeMatrix //
        , fieldBeanParam.bpeInner.innerMatrix //
        , fieldBeanParam.bpePath //
        , fieldBeanParam.bpeInner.innerPath //
        , fieldBeanParam.bpeQuery //
        , fieldBeanParam.bpeInner.innerQuery //
        , content //
        , fieldBeanParam.bpeCookie //
        , fieldBeanParam.bpeInner.innerCookie);
  }

  @POST
  @Path("allparam/{bpePath}/{innerPath}")
  public String everyParam(String content, @BeanParam BeanParamEntity bean) {
    return appnd(bean.bpeForm //
        , bean.bpeInner.innerForm //
        , bean.bpeHeader //
        , bean.bpeInner.innerHeader //
        , bean.bpeMatrix //
        , bean.bpeInner.innerMatrix //
        , bean.bpePath //
        , bean.bpeInner.innerPath //
        , bean.bpeQuery //
        , bean.bpeInner.innerQuery //
        , content //
        , bean.bpeCookie //
        , bean.bpeInner.innerCookie);
  }

  private static String appnd(String... strings) {
    StringBuilder sb = new StringBuilder();
    if (strings != null)
      for (String s : strings)
        sb.append(s);
    return sb.toString();
  }

}
