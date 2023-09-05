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

package ee.jakarta.tck.ws.rs.common.client;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import ee.jakarta.tck.ws.rs.common.webclient.TestFailureException;
import ee.jakarta.tck.ws.rs.common.webclient.validation.CheckOneOfStatusesTokenizedValidator;
import ee.jakarta.tck.ws.rs.lib.util.BASE64Encoder;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JaxrsWebTestCase.Execution;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class JaxrsCommonClient extends JAXRSCommonClient {

  private static final long serialVersionUID = 1L;

  protected transient JaxrsWebTestCase testCase;

  protected boolean isTestCaseAfterInvocation;

  // list of clients to be closed automatically at cleanup
  protected List<Client> clients = new LinkedList<Client>();

  /**
   * <PRE>
   * Sets the appropriate test properties based
   * on the values stored in TEST_PROPS
   * </PRE>
   */
  protected void setTestProperties(JaxrsWebTestCase testCase) {
    TestUtil.logTrace("[JAXRSCommonClient] setTestProperties");

    if (TEST_PROPS.get(Property.STATUS_CODE) == null)
      setProperty(Property.STATUS_CODE, getStatusCode(Response.Status.OK));
    setWebTestCaseProperties(testCase);
  }

  protected void setWebTestCaseProperties(JaxrsWebTestCase testCase) {
    Property key = null;
    String value = null;
    // process the remainder of the properties
    for (Enumeration<Property> e = TEST_PROPS.keys(); e.hasMoreElements();) {
      key = e.nextElement();
      value = TEST_PROPS.get(key);
      switch (key) {
      case APITEST:
        break;
      case BASIC_AUTH_PASSWD:
      case BASIC_AUTH_REALM:
        break;
      case BASIC_AUTH_USER:
        String user = TEST_PROPS.get(Property.BASIC_AUTH_USER);
        String password = TEST_PROPS.get(Property.BASIC_AUTH_PASSWD);
        String base64 = new BASE64Encoder()
            .encode((user + ":" + password).getBytes());
        testCase.addHeader("Authorization", " Basic " + base64);
        break;
      case CONTENT:
        // req.setContent(value);
        testCase.setEntity(value);
        break;
      case DONOTUSEServletName:
        break;
      case EXPECT_RESPONSE_BODY:
        // FIXME
        // setExpectResponseBody(false);
        break;
      case EXPECTED_HEADERS:
        testCase.addExpectedHeader(value);
        break;
      case FOLLOW_REDIRECT:
        TestUtil.logTrace("##########Found redirect Property");
        _redirect = true;
        break;
      case GOLDENFILE:
        StringBuffer sb = new StringBuffer(50);
        sb.append(_tsHome).append(GOLDENFILEDIR);
        sb.append(_generalURI).append(SL);
        sb.append(value);
        testCase.setGoldenFilePath(sb.toString());
        break;
      case IGNORE_BODY:
        // FIXME
        // setIgnoreResponseBody(true);
        testCase.setGoldenFilePath(null);
        break;
      case IGNORE_STATUS_CODE:
        testCase.setExpectedStatusCode("-1");
        break;
      case REASON_PHRASE:
        testCase.setExpectedReasonPhrase(value);
        break;
      case REQUEST:
        testCase.setUrlRequest(value);
        break;
      case REQUEST_HEADERS:
        String[] headers = splitByColon(value);
        for (String header : headers) {
          String[] split = header.split(":", 2);
          testCase.addHeader(split[0].trim(), split[1].trim());
        }
        break;
      case RESPONSE_MATCH:
        // setResponseMatch(TEST_PROPS.getProperty(key));
        break;
      case SAVE_STATE:
        _saveState = true;
        break;
      case SEARCH_STRING:
        value = testCase.getTextCaser().getCasedText(value);
        testCase.setResponseSearchString(value);
        break;
      case SEARCH_STRING_IGNORE_CASE:
        testCase.setResponseSearchStringIgnoreCase(value);
        break;
      case STANDARD:
        break;
      case STATUS_CODE:
        if (value.contains("|"))
          testCase.setStrategy(
              CheckOneOfStatusesTokenizedValidator.class.getName());
        testCase.setExpectedStatusCode(value);
        break;
      case STRATEGY:
        testCase.setStrategy(value);
        break;
      case TEST_NAME:
        // testName = TEST_PROPS.getProperty(key);
        break;
      case UNEXPECTED_HEADERS:
        testCase.addUnexpectedHeader(value);
        break;
      case UNEXPECTED_RESPONSE_MATCH:
        testCase.setUnexpectedResponseSearchString(value);
        break;
      case UNORDERED_SEARCH_STRING:
        value = testCase.getTextCaser().getCasedText(value);
        testCase.setUnorderedSearchString(value);
        break;
      case USE_SAVED_STATE:
        _useSavedState = true;
        break;
      }
    }
  }

  /**
   * Replaces String#split("|"), as it does not split for special character '|'
   */
  protected static String[] splitByColon(String value) {
    int colonIndex = -1, lastIndex = 0;
    LinkedList<String> list = new LinkedList<String>();
    while ((colonIndex = value.indexOf('|', lastIndex)) != -1) {
      list.add(value.substring(lastIndex, colonIndex));
      lastIndex = colonIndex + 1;
    }
    if (lastIndex < value.length())
      list.add(value.substring(lastIndex));
    return list.toArray(new String[list.size()]);
  }

  /**
   * <PRE>
   * Invokes a test based on the properties
   * stored in TEST_PROPS.  Once the test has completed,
   * the properties in TEST_PROPS will be cleared.
   * </PRE>
   *   
   *  @throws Fault
   *           If an error occurs during the test run
   */
  protected void invoke() throws Fault {
    TestUtil.logTrace("[JAXRSCommonClient] invoke");
    try {
      getTestCase().setPort(_port);
      getTestCase().setHostname(_hostname);
      setTestProperties(testCase);
      TestUtil.logTrace("[JAXRSCommonClient] EXECUTING");
      if (_useSavedState && _state != null) {
        testCase.getRequest().setState(_state);
      }
      if (_redirect != false) {
        TestUtil.logTrace("##########Call setFollowRedirects");
        testCase.getRequest().setFollowRedirects(_redirect);
      }
      testCase.execute();
      isTestCaseAfterInvocation = true;
      if (_saveState) {
        _state = testCase.getResponse().getState();
      }
    } catch (TestFailureException tfe) {
      Throwable t = tfe.getRootCause();
      if (t != null) {
        TestUtil.logErr("Root cause of Failure: " + t.getMessage(), t);
      }
      throw new Fault("[JAXRSCommonClient] " + _testName
          + " failed!  Check output for cause of failure.", tfe);
    } finally {
      _useSavedState = false;
      _saveState = false;
      _redirect = false;
      clearTestProperties();
      clients.add(testCase.client);
    }
  }

  @Override
  public void cleanup() throws Fault{
    super.cleanup();
    // The client.close has to be called on cleanup, because after invoke,
    // some methods are called and resources might not be available then
    // (javadoc: Close client instance and all it's associated resources).
    // Since more invoke() invocations are possible, the clients are stored
    // in a list to be closed on cleanup
    for (Client c : clients)
      c.close();
    clients.clear();
  }

  public void setup() {
    super.setup();
    String property = System.getProperty("cts.tmp", "/tmp");
    if (property != null)
      System.setProperty("java.io.tmpdir", property);
  }

  protected JaxrsWebTestCase getTestCase() {
    if (testCase == null || isTestCaseAfterInvocation) {
      testCase = new JaxrsWebTestCase();
      isTestCaseAfterInvocation = false;
    }
    return testCase;
  }

  @Override
  protected String buildRequest(Request type, String... path) {
    getTestCase().setRequestType(type.name());
    StringBuilder sb = new StringBuilder();
    sb.append(_contextRoot == null ? "" : _contextRoot).append(SL);
    for (String segment : path)
      sb.append(segment);
    return sb.toString();
  }

  @Override
  @Deprecated
  protected void setProperty(String key, String value) {
    super.setProperty(key, value);
  }

  @Override
  @Deprecated
  protected String buildRequest(String type, String... path) {
    return super.buildRequest(type, path);
  }

  protected Response getResponse() {
    return testCase.getJaxrsResponse();
  }

  @Override
  protected String getResponseBody() {
    return getResponseBody(String.class);
  }

  protected <T> T getResponseBody(Class<T> clazz) {
    return getResponse().readEntity(clazz);
  }

  public List<Object> getProvidersToRegister() {
    return getTestCase().getProvidersToRegister();
  }

  public void addProvider(Object providerToRegister) {
    getTestCase().addProviderToRegister(providerToRegister);
  }

  @Override
  protected String[] getResponseHeaders() throws Fault {
    return getMetadata(getResponse().getMetadata());
  }

  /**
   * Calls setHeader(Property.REQUEST_HEADERS, header : values);
   * 
   * @param header
   *          Recommended one of HttpHeaders static values
   * @param values
   *          the value[1]value[2]...value[n] which are to be assigned to
   *          {@code}header name
   */
  protected void addHeader(String header, String... values) {
    StringBuilder sb = new StringBuilder();
    sb.append(header).append(":");
    if (values != null)
      for (String value : values)
        sb.append(value);
    setProperty(Property.REQUEST_HEADERS, sb.toString());
  }

  /**
   * This method is typically used to transform http headers metadata into a
   * String array. The headers are in a form of java class instance, e.g.
   * stream(!), or String
   * 
   * @param metadata
   * @return
   */
  public static String[] getMetadata(MultivaluedMap<String, Object> metadata) {
    String[] headers = new String[metadata.size()];
    int i = 0;
    for (Entry<String, List<Object>> e : metadata.entrySet()) {
      headers[i++] = e.getKey() + ":" + listToString(e.getValue());
    }
    return headers;
  }

  @Override
  protected Status getResponseStatusCode() {
    return Status.fromStatusCode(getResponse().getStatus());
  }

  protected void setRequestContentEntity(Object object) {
    getTestCase().setEntity(object);
  }

  public static <T> String listToString(List<T> list) {
    StringBuilder sb = new StringBuilder();
    for (T s : list)
      sb.append(s).append(" ");
    return sb.toString().trim();
  }

  protected void printClientCall(boolean print) {
    getTestCase().setPrintClientCall(print);
  }

  protected void setAsynchronousProcessing() {
    getTestCase().setProcessingType(Execution.ASYNCHRONOUS);
  }

  protected void setPrintEntity(boolean printEntity) {
    getTestCase().setPrintEntity(printEntity);
  }

  protected void bufferEntity(boolean buffer) {
    getTestCase().bufferEntity(buffer);
  }

  protected void setTextCaser(TextCaser caser) {
    getTestCase().setTextCaser(caser);
  }

}
