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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.ee.rs.Constants;
import ee.jakarta.tck.ws.rs.ee.rs.JaxrsParamClient;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;

import jakarta.ws.rs.core.MediaType;
import static org.junit.jupiter.api.Assertions.assertTrue;
/*
 * @since 2.0.1
 */
public abstract class BeanParamCommonClient extends JaxrsParamClient {
  private static final long serialVersionUID = 201L;

  protected String fieldBeanParam;

  protected int exceptionInEntity = 0;

  private void setFieldOrBean(String prefix) {
    fieldBeanParam = prefix.contains(ParamTest.FIELD) ? ParamTest.FIELD
        : ParamTest.PARAM;
  }

  protected void fieldOrParamEntityName(Class<?> entityClazz,
      String nonDefaultValue, String... prefix) throws Fault {
    setFieldOrBean(prefix[0]);
    if (prefix[0].contains(ParamTest.FIELD))
      prefix[0] = "";
    super.fieldOrParamEntityName(entityClazz, nonDefaultValue, prefix);
  }

  protected void fieldOrParamThrowingWebApplicationExceptionTest(String prefix)
      throws Fault {
    setFieldOrBean(prefix);
    exceptionInEntity++;
    super.fieldOrParamThrowingWebApplicationExceptionTest("");
  }

  @Override
  protected void fieldOrParamThrowingIllegalArgumentExceptionTest(String prefix)
      throws Fault {
    setFieldOrBean(prefix);
    exceptionInEntity++;
    super.fieldOrParamThrowingIllegalArgumentExceptionTest("");
  }

  @Override
  protected void fieldOrParamEncodedTest(String prefix) throws Fault {
    setFieldOrBean(prefix);
    StringBuilder request = new StringBuilder();
    request.append(ParamEntityWithValueOf.class.getSimpleName());
    setProperty(Property.REQUEST, buildRequest(request.toString() + "=%21"));
    setProperty(Property.SEARCH_STRING, fieldBeanParam, request.toString(),
        "=!");
    setProperty(Property.SEARCH_STRING, Constants.INNER, fieldBeanParam,
        request.toString(), "=!");
    invoke();

    request = new StringBuilder();
    request.append(ParamEntityWithFromString.class.getSimpleName());
    request.append("=%21");
    paramEntity(request.toString());
  }

  protected void paramEntity(String request) throws Fault {
    if (searchEqualsEncoded) {
      setProperty(Property.SEARCH_STRING_IGNORE_CASE, fieldBeanParam,
          request.replace("=", "%3d"));
      setProperty(Property.SEARCH_STRING_IGNORE_CASE, Constants.INNER,
          fieldBeanParam, request.replace("=", "%3d"));
    } else {
      setProperty(Property.SEARCH_STRING, fieldBeanParam, request);
      setProperty(Property.SEARCH_STRING, Constants.INNER, fieldBeanParam,
          request);
    }
    buildRequestAndInvoke(request);
  }

  @Override
  protected String getDefaultValueOfParam(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(fieldBeanParam).append(param).append("=");
    sb.append(Constants.DEFAULT_VALUE);
    sb.append("|").append(Constants.INNER).append(fieldBeanParam);
    sb.append(param).append("=");
    sb.append(Constants.DEFAULT_VALUE);
    return sb.toString();
  }

  @Override
  protected void invoke() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    setProperty(Property.REQUEST_HEADERS, buildAccept(MediaType.WILDCARD_TYPE));
    super.invoke();
  }

  @Override
  protected void buildRequestAndInvoke(String request) throws Fault {
    String req = exceptionInEntity == 0 ? buildRequest(request)
        : buildRequestForException(request, exceptionInEntity);
    setProperty(Property.REQUEST, req);
    invoke();
  }

  /**
   * @param entity
   *          specifies whether the exception is to be tested in first entity,
   *          i.e. in the master BeanParamEntity or the second entity
   *          Inner*BeanParamEntity
   */
  protected String buildRequestForException(String param, int entity)
      throws Fault {
    return buildRequest(param);
  }

  // Cookie Param

  protected void createAndCheckCookie(String param, String uri) throws Fault {
    // create cookie
    String requestForCookie = param + ";" + Constants.INNER + param;
    setProperty(Property.CONTENT, requestForCookie);
    setProperty(Property.REQUEST, buildRequest(Request.POST, uri));
    setProperty(Property.SAVE_STATE, "true");
    invoke();
    checkCookie(param);
    // check cookie
    setProperty(Property.USE_SAVED_STATE, "true");
  }

  protected void checkCookie(String cookie) throws Fault {
    boolean found = false;
    String lowCookie = stripQuotesSpacesAndLowerCase(cookie);
    String[] headers = getResponseHeaders();
    for (String h : headers) {
      String header = stripQuotesSpacesAndLowerCase(h);
      if (header.startsWith("set-cookie"))
        if (header.contains(lowCookie))
          found = true;
    }
    assertTrue(found, "Could not find cookie" + cookie+ "in response headers:" +
        JaxrsUtil.iterableToString(";", headers));
    logMsg("Found cookie", cookie, "as expected");
  }

  protected static String stripQuotesSpacesAndLowerCase(String cookie) {
    return cookie.toLowerCase().replace("\"", "").replace("'", "").replace(" ",
        "");
  }
}
