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

package ee.jakarta.tck.ws.rs.ee.rs;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

import jakarta.ws.rs.core.Response.Status;

/**
 * @since 2.0
 */
public abstract class JaxrsParamClient extends JAXRSCommonClient {

  private static final long serialVersionUID = 200L;

  /**
   * Currently, not for every param annotation @DefaultValue works. Where it
   * works not, set useDefaultValue to false when inherit
   */
  protected boolean useDefaultValue = true;

  /**
   * When URI is to be encoded, equals ('=') is encoded as '%3d' This variable
   * is to set which equals, the original or encoded is to be searched in
   * response
   */
  protected boolean searchEqualsEncoded = false;

  /**
   * Behind a test, there would be the following logic. a) There is a field
   * test. This is to test instance attributes, i.e. fields. b) There is a param
   * test. This is to test argument attributes, i.e. method arguments. Thus, the
   * argument shall be named only by an Entity name (i.e. the name of the entity
   * in jaxrs.ee.rs package), the CollectionName prefix to test arguments, or
   * the Field prefix to check variant a, or Field and CollectionName to check
   * variant a with collections.
   * 
   * @throws Fault
   *           : When test fail
   */
  protected void fieldOrParamEntityName(Class<?> entityClazz,
      String nonDefaultValue, String... prefix) throws Fault {
    StringBuilder request = new StringBuilder();
    // add prefix
    for (String p : prefix)
      request.append(p);

    request.append(entityClazz.getSimpleName());

    if (useDefaultValue)
      paramEntityDefault(request.toString());

    request.append("=").append(nonDefaultValue);
    paramEntity(request.toString());
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.2; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void paramEntityWithConstructorTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithConstructor.class, "JAXRS_SPEC_5.2",
        "");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.3; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void paramEntityWithValueOfTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithValueOf.class, "JAXRS_SPEC_5.3", "");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.3; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void paramEntityWithFromStringTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithFromString.class, "JAXRS_SPEC_5.3",
        "");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.4; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  /**
   * @param collection
   *          : Collection to use from {List,Set,SortedSet}
   * @throws Fault
   */
  protected void paramCollectionEntityWithFromStringTest(
      CollectionName collection) throws Fault {
    fieldOrParamEntityName(ParamEntityWithFromString.class, "JAXRS_SPEC_5.4",
        collection.value);
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void fieldEntityWithConstructorTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithConstructor.class, "JAXRS_SPEC_5.2",
        ParamTest.FIELD);
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void fieldEntityWithValueOfTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithValueOf.class, "JAXRS_SPEC_5.3",
        ParamTest.FIELD);
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  public void fieldEntityWithFromStringTest() throws Fault {
    fieldOrParamEntityName(ParamEntityWithFromString.class, "JAXRS_SPEC_5.3",
        ParamTest.FIELD);
  }

  /*
   * @assertion_ids: JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named Param is handled properly
   */
  /**
   * @param collection
   *          : Collection to use from {List,Set,SortedSet}
   * @throws Fault
   */
  protected void fieldCollectionEntityWithFromStringTest(
      CollectionName collection) throws Fault {
    fieldOrParamEntityName(ParamEntityWithFromString.class, "JAXRS_SPEC_5.4",
        ParamTest.FIELD, collection.value);
  }

  protected void fieldOrParamEncodedTest(String prefix) throws Fault {
    StringBuilder request = new StringBuilder();
    request.append(prefix);
    request.append(ParamEntityWithValueOf.class.getSimpleName());
    setProperty(Property.REQUEST, buildRequest(request.toString() + "=%21"));
    setProperty(Property.SEARCH_STRING, request.toString() + "=!");
    invoke();

    request = new StringBuilder();
    request.append(prefix);
    request.append(ParamEntityWithFromString.class.getSimpleName());
    request.append("=%21");
    paramEntity(request.toString());
  }

  /*
   * @assertion_ids: JAXRS:SPEC:12.2;
   * 
   * @test_Strategy: Verify that named Param @Encoded is handled
   */
  public void paramEntityWithEncodedTest() throws Fault {
    fieldOrParamEncodedTest("");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:7;
   * 
   * @test_Strategy: Verify that named Param @Encoded is handled
   */
  public void fieldEntityWithEncodedTest() throws Fault {
    fieldOrParamEncodedTest(ParamTest.FIELD);
  }

  /*
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  protected void fieldOrParamThrowingWebApplicationExceptionTest(String prefix)
      throws Fault {
    StringBuilder request = new StringBuilder();
    request.append(prefix);
    request.append(
        ParamEntityThrowingWebApplicationException.class.getSimpleName());
    request.append("=").append(Status.CREATED.name());
    // to override
    paramEntityThrowingAfterRequestSet(request.toString());
    setProperty(Property.SEARCH_STRING,
        WebApplicationExceptionMapper.class.getSimpleName());
    setProperty(Property.SEARCH_STRING, Status.CREATED.name());
    buildRequestAndInvoke(request.toString());
  }

  /*
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  protected void paramThrowingWebApplicationExceptionTest() throws Fault {
    fieldOrParamThrowingWebApplicationExceptionTest("");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  protected void fieldThrowingWebApplicationExceptionTest() throws Fault {
    fieldOrParamThrowingWebApplicationExceptionTest(ParamTest.FIELD);
  }

  protected void paramEntityThrowingAfterRequestSet(String request)
      throws Fault {
  }

  protected void fieldOrParamThrowingIllegalArgumentExceptionTest(String prefix)
      throws Fault {
    StringBuilder request = new StringBuilder();
    request.append(prefix);
    request
        .append(ParamEntityThrowingExceptionGivenByName.class.getSimpleName());
    request.append("=").append(IllegalArgumentException.class.getName());
    // to override
    paramEntityThrowingAfterRequestSet(request.toString());
    setProperty(Property.SEARCH_STRING,
        WebApplicationExceptionMapper.class.getSimpleName());
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        ParamEntityThrowingExceptionGivenByName.ERROR_MSG);
    setProperty(Property.SEARCH_STRING,
        IllegalArgumentException.class.getName());
    buildRequestAndInvoke(request.toString());
  }

  /*
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  public void paramThrowingIllegalArgumentExceptionTest() throws Fault {
    fieldOrParamThrowingIllegalArgumentExceptionTest("");
  }

  /*
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.1; JAXRS:SPEC:9.2;
   * JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   * 
   * if the field or property is annotated with @MatrixParam,
   * 
   * @QueryParam or @PathParam then an implementation MUST generate a
   * WebApplicationException that wraps the thrown exception with a not found
   * response (404 status) and no entity;
   *
   * if the field or property is annotated with @HeaderParam or @CookieParam
   * then an implementation MUST generate a WebApplicationException that wraps
   * the thrown exception with a client error response (400 status) and no
   * entity.
   */
  public void fieldThrowingIllegalArgumentExceptionTest() throws Fault {
    fieldOrParamThrowingIllegalArgumentExceptionTest(ParamTest.FIELD);
  }

  protected void buildRequestAndInvoke(String request) throws Fault {
    setProperty(Property.REQUEST, buildRequest(request));
    invoke();
  }

  protected void paramEntityDefault(String request) throws Fault {
    setProperty(Property.SEARCH_STRING, getDefaultValueOfParam(request));
    setProperty(Property.REQUEST, buildRequest(""));
    invoke();
  }

  protected void paramEntity(String request) throws Fault {
    if (searchEqualsEncoded)
      setProperty(Property.SEARCH_STRING_IGNORE_CASE,
          request.replace("=", "%3d"));
    else
      setProperty(Property.SEARCH_STRING, request);
    buildRequestAndInvoke(request);
  }

  protected abstract String buildRequest(String param);

  protected abstract String getDefaultValueOfParam(String param);

  public enum CollectionName {
    LIST("List"), SET("Set"), SORTED_SET("SortedSet");

    private String value;

    public String value() {
      return value;
    }

    private CollectionName(String value) {
      this.value = value;
    }
  };

  protected static String segmentFromParam(String param) {
    return param.contains("=") ? param.replaceAll("=.*", "") : param;
  }

}
