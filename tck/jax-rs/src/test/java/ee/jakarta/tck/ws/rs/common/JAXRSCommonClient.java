 /*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.common;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;


import ee.jakarta.tck.ws.rs.common.util.Data;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.common.webclient.TestFailureException;
import ee.jakarta.tck.ws.rs.common.webclient.WebTestCase;
import ee.jakarta.tck.ws.rs.common.webclient.http.HttpRequest;
import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import ee.jakarta.tck.ws.rs.common.webclient.validation.CheckOneOfStatusesTokenizedValidator;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpState;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * 
 * @author dianne jiao
 * @author jan supol
 */
//public abstract class JAXRSCommonClient extends ServiceEETest {
public abstract class JAXRSCommonClient {
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1L;

  /**
   * TS Webserver host property
   */
  protected static final String SERVLETHOSTPROP = "webServerHost";

  /**
   * TS Webserver port property
   */
  protected static final String SERVLETPORTPROP = "webServerPort";

  /**
   * TS home property
   */
  protected static final String TSHOME = "ts_home";

  /**
   * Test properties
   */
  protected static final Hashtable<Property, String> TEST_PROPS = new Hashtable<Property, String>();

  /**
   * StatusCode property
   */
  protected static final String STATUS_CODE = "STATUS_CODE";

  /**
   * Request property
   */
  protected static final String REQUEST = "REQUEST";

  /**
   * Request headers property
   */
  protected static final String REQUEST_HEADERS = "REQUEST_HEADERS";

  /**
   * Goldenfile property
   */
  protected static final String GOLDENFILE = "goldenfile";

  /**
   * Search string property
   */
  protected static final String SEARCH_STRING = "SEARCH_STRING";

  /**
   * Search string case insensitive property
   */
  protected String TESTDIR = null;

  /**
   * Goldenfile directory
   */
  protected String GOLDENFILEDIR = "/src/web";

  /**
   * Default request method
   */
  protected static final String GET = "GET ";

  /**
   * HTTP 1.0
   */
  protected static final String HTTP10 = " HTTP/1.0";

  /**
   * HTTP 1.1
   */
  protected static final String HTTP11 = " HTTP/1.1";

  /**
   * Forward slash
   */
  protected static final String SL = "/";

  /**
   * Goldenfile suffix
   */
  protected static final String GF_SUFFIX = ".gf";

  /**
   * JSP suffix
   */
  /**
   * Current test name
   */
  protected String _testName = null;

  /**
   * location of _tsHome
   */
  protected String _tsHome = null;

  /**
   * Context root of target tests
   */
  public String _contextRoot = null;

  /**
   * General file/request URI for both gfiles and tests
   */
  protected String _generalURI = null;

  /**
   * Target webserver hostname
   */
  protected String _hostname = null;

  /**
   * Target webserver port
   */
  protected int _port = 0;

  /**
   * HttpState that may be used for multiple invocations requiring state.
   */
  protected HttpState _state = null;

  /**
   * Test case.
   */
  protected WebTestCase _testCase = null;

  /**
   * Use saved state.
   */
  protected boolean _useSavedState = false;

  /**
   * Save state.
   */
  protected boolean _saveState = false;

  protected boolean _redirect = false;

  public static final String newline = System.getProperty("line.separator");

  public static final String servletAdaptor = System.getProperty("servlet_adaptor", "org.glassfish.jersey.servlet.ServletContainer");

  public static final String indent = "    ";

  /**
   * List of possible requests
   */
  protected enum Request {
    GET, PUT, POST, HEAD, OPTIONS, DELETE, TRACE
  }

  /**
   * the list of properties to be put into a property table
   */
  protected enum Property {
    APITEST, BASIC_AUTH_PASSWD, BASIC_AUTH_REALM, BASIC_AUTH_USER, //
    CONTENT, DONOTUSEServletName, EXPECT_RESPONSE_BODY, EXPECTED_HEADERS, //
    FOLLOW_REDIRECT, GOLDENFILE, IGNORE_BODY, IGNORE_STATUS_CODE, //
    REASON_PHRASE, REQUEST, REQUEST_HEADERS, RESPONSE_MATCH, SAVE_STATE, //
    SEARCH_STRING, SEARCH_STRING_IGNORE_CASE, STANDARD, STATUS_CODE, //
    STRATEGY, TEST_NAME, UNEXPECTED_HEADERS, UNEXPECTED_RESPONSE_MATCH, //
    UNORDERED_SEARCH_STRING, USE_SAVED_STATE;
  }

  /*
   * public methods
   * ========================================================================
   */
  /**
   * <code>setTestDir</code> sets the current test directory.
   * 
   * @param testDir
   *          a <code>String</code> value
   */
  public void setTestDir(String testDir) {
    TestUtil.logTrace("[JAXRSCommonClient] setTestDir");
    TESTDIR = testDir;
  }

  public void setContextRoot(String root) {
    TestUtil.logTrace("[JAXRSCommonClient] Contextroot set at " + root);
    _contextRoot = root;
  }

  public String getContextRoot() {
    TestUtil.logTrace("[JAXRSCommonClient]getContextRoot");
    return _contextRoot;
  }

  /**
   * <code>setup</code> is by the test harness to initialize the tests.
   * 
   * @param args
   *          a <code>String[]</code> value
   * @param p
   *          a <code>Properties</code> value
   * @exception Fault
   *              if an error occurs
   */
  //public void setup(String[] args, Properties p)   {
  public void setup()   {
    TestUtil.logTrace("setup method JAXRSCommonClient");

    String hostname = System.getProperty(SERVLETHOSTPROP);
    String portnum = System.getProperty(SERVLETPORTPROP);
    //String tshome = p.getProperty(TSHOME);

    assertTrue(!isNullOrEmpty(hostname),
        "[JAXRSCommonClient] 'webServerHost' was not set.");
    _hostname = hostname.trim();
    assertTrue(!isNullOrEmpty(portnum),
        "[JAXRSCommonClient] 'webServerPort' was not set.");
    _port = Integer.parseInt(portnum.trim());

    //assertTrue(!isNullOrEmpty(tshome),
    //    "[JAXRSCommonClient] 'tshome' was not set in the build.properties.");
    //_tsHome = tshome.trim();

    TestUtil.logMsg("[JAXRSCommonClient] Test setup OK");
  }

  /**
   * <code>cleanup</code> is called by the test harness to cleanup after text
   * execution
   * 
   * @exception Fault
   *              if an error occurs
   */
  public void cleanup() throws Fault  {
    TestUtil.logMsg("[JAXRSCommonClient] Test cleanup OK");
  }

  /*
   * protected methods
   * ========================================================================
   */
  /**
   * <PRE>
   * Invokes a test based on the properties
   * stored in TEST_PROPS.  Once the test has completed,
   * the properties in TEST_PROPS will be cleared.
   * </PRE>
   * 
   * @throws Fault
   *           If an error occurs during the test run
   */
  protected void invoke() throws Fault {
    TestUtil.logTrace("[JAXRSCommonClient] invoke");
    try {
      _testCase = new WebTestCase();
      setTestProperties(_testCase);
      TestUtil.logTrace("[JAXRSCommonClient] EXECUTING");
      if (_useSavedState && _state != null) {
        _testCase.getRequest().setState(_state);
      }
      if (_redirect != false) {
        TestUtil.logTrace("##########Call setFollowRedirects");
        _testCase.getRequest().setFollowRedirects(_redirect);
      }
      _testCase.execute();
      if (_saveState) {
        _state = _testCase.getResponse().getState();
      }
    } catch (TestFailureException tfe) {
      Throwable t = tfe.getRootCause();
      if (t != null) {
        TestUtil.logErr("Root cause of Failure: " + t.getMessage(), t);
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        } else if (t instanceof Error) {
          throw (Error) t;
        } else {
          throw new RuntimeException(t);
        }
      }
      throw new Fault("[JAXRSCommonClient] " + _testName
          + " failed!  Check output for cause of failure.", tfe);
    } finally {
      _useSavedState = false;
      _saveState = false;
      _redirect = false;
      clearTestProperties();
    }
  }

  /**
   * <PRE>
   * Sets the appropriate test properties based
   * on the values stored in TEST_PROPS
   * </PRE>
   */
  protected void setTestProperties(WebTestCase testCase) {
    TestUtil.logTrace("[JAXRSCommonClient] setTestProperties");

    setStandardProperties(TEST_PROPS.get(Property.STANDARD), testCase);
    setApiTestProperties(TEST_PROPS.get(Property.APITEST), testCase);

    if (TEST_PROPS.get(Property.STATUS_CODE) == null)
      setProperty(Property.STATUS_CODE, getStatusCode(Response.Status.OK));

    HttpRequest req = testCase.getRequest();

    // Check for a request object. If doesn't exist, then
    // check for a REQUEST property and create the request object.
    if (req == null)
      req = setWebTestCaseRequest(testCase, null);
    setWebTestCaseProperties(testCase, req);
  }

  protected HttpRequest setWebTestCaseRequest(WebTestCase testCase,
      HttpRequest req) {
    String request = TEST_PROPS.get(Property.REQUEST);
    boolean isRequest = false;
    for (Request r : Request.values())
      if (request.startsWith(r.name()))
        isRequest = true;
    if (request.endsWith(HTTP10) || request.endsWith(HTTP11))
      isRequest = true;
    if (isRequest) {
      // user has overridden default request behavior
      req = createHttpRequest(request, _hostname, _port);
      testCase.setRequest(req);
    } else {
      req = createHttpRequest(getTSRequest(request), _hostname, _port);
      testCase.setRequest(req);
    }
    return req;
  }

  protected void setWebTestCaseProperties(WebTestCase testCase,
      HttpRequest req) {
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
      case BASIC_AUTH_USER:
        String user = TEST_PROPS.get(Property.BASIC_AUTH_USER);
        String password = TEST_PROPS.get(Property.BASIC_AUTH_PASSWD);
        String realm = TEST_PROPS.get(Property.BASIC_AUTH_REALM);
        req.setAuthenticationCredentials(user, password,
            HttpRequest.BASIC_AUTHENTICATION, realm);
        break;
      case CONTENT:
        req.setContent(value);
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
        break;
      case REQUEST_HEADERS:
        req.addRequestHeader(value);
        break;
      case RESPONSE_MATCH:
        // setResponseMatch(TEST_PROPS.getProperty(key));
        break;
      case SAVE_STATE:
        _saveState = true;
        break;
      case SEARCH_STRING:
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
        testCase.setUnorderedSearchString(value);
        break;
      case USE_SAVED_STATE:
        _useSavedState = true;
        break;
      }
    }
  }

  /**
   * Create request <type> /<contextroot>/<path> HTTP/1.1. ContextRoot is
   * defined in every client.
   * 
   * @param type
   *          PUT, GET, POST, ...
   * @param path
   *          path defined in a servlet
   * @return String representing HTTP request
   */
  protected String buildRequest(String type, String... path) {
    StringBuilder sb = new StringBuilder();
    sb.append(type).append(" ").append(_contextRoot).append(SL);
    for (String segment : path)
      sb.append(segment);
    sb.append(HTTP11);
    return sb.toString();
  }

  protected String buildRequest(Request type, String... path) {
    return buildRequest(type.name(), path);
  }

  protected String buildRequest10(Request type, String... path) {
    return buildRequest(type, path).replace(HTTP11, HTTP10);
  }

  /**
   * Create counterpart to @Produces
   * 
   * @param type
   * @return Accept:{@code type}.{@link #toString()}
   */
  protected static String buildAccept(MediaType type) {
    return buildHeaderMediaType("Accept", type);
  }

  /**
   * Create counterpart to @Consumes
   * 
   * @param type
   * @return
   */
  protected static String buildContentType(MediaType type) {
    return buildHeaderMediaType("Content-Type", type);
  }

  protected static String buildHeaderMediaType(String header, MediaType type) {
    StringBuilder sb = new StringBuilder();
    sb.append(header).append(":").append(type.getType()).append(SL);
    sb.append(type.getSubtype());
    return sb.toString();
  }

  public static String toString(InputStream inStream) throws IOException{
    try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8))) {
      return bufReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  public static String editWebXmlString(InputStream inStream) throws IOException{
    return toString(inStream).replaceAll("servlet_adaptor", servletAdaptor);
  }

  /**
   * @return http response body as string
   * @throws Fault
   *           when an error occur
   */
  protected String getResponseBody() throws Fault  {
    try {
      HttpResponse response;
      response = _testCase.getResponse();
      boolean isNull = response.getResponseBodyAsRawStream() == null;
      return isNull ? null : response.getResponseBodyAsString();
    } catch (IOException e) {
        throw new Fault(e);
    }
  }

  /**
   * @return http response body as string
   * @throws Fault
   *           when an error occur
   */
  protected String[] getResponseHeaders() throws Fault  {
    Header[] headerEntities = _testCase.getResponse().getResponseHeaders();
    String[] headers = new String[headerEntities.length];
    for (int i = 0; i != headerEntities.length; i++)
      headers[i] = headerEntities[i].toString();
    return headers;
  }

  /**
   * @param s
   *          the header to search
   * @throws Fault
   *           when header not found
   */
  protected void assertResponseHeadersContain(String s) throws Fault  {
    boolean found = false;
    for (String header : getResponseHeaders())
      if (header.contains(s)) {
        found = true;
        break;
      }
    assertTrue(found, "Response headers do not contain"+ s);
  }

  /**
   * @param s
   *          the entity to search
   * @throws Fault
   *           when entity not found
   */
  protected void assertResponseBodyContain(String s)  throws Fault {
    boolean found = getResponseBody().contains(s);
    assertTrue(found, "Response body does not contain"+ s);
  }

  /**
   * get HttpResponse#statusCode
   * 
   * @return JAXRS Response.Status equivalent of HttpResponse#statusCode
   */
  protected Response.Status getResponseStatusCode() {
    String status = _testCase.getResponse().getStatusCode();
    return Response.Status.fromStatusCode(Integer.parseInt(status));
  }

  /**
   * Set TEST_PROPS property value. If it already exists, the value is appended
   */
  protected void setProperty(String key, String value) {
    Property property = Property.valueOf(key);
    setProperty(property, value);
  }

  /*
   * @since 2.0.1
   */
  protected void setProperty(Property key, String... value) {
    setProperty(key, objectsToString("", (Object[]) value));
  }

  protected void setProperty(Property key, String value) {
    String oldValue = TEST_PROPS.get(key);
    if (oldValue == null) {
      TEST_PROPS.put(key, value);
    } else {
      int len = value.length() + oldValue.length() + 1;
      StringBuilder combinedValue = new StringBuilder(len);
      combinedValue.append(oldValue).append("|").append(value);
      TEST_PROPS.put(key, combinedValue.toString());
    }
  }

  protected void clearProperty(Property key) {
    TEST_PROPS.remove(key);
  }

  /**
   * This pattern is used in all subclasses
   */
  //protected Status run(String[] args) {
  //  Status s;
  //  s = run(args, new PrintWriter(System.out), new PrintWriter(System.err));
  //  s.exit();
  //  return s;
  //}

  /**
   * Asserts that a condition is true.
   * 
   * @param conditionTrue
   *          tested condition
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   * @ 
   *           when conditionTrue is not met with message provided
   */
  /*public static void //
      assertFault(boolean conditionTrue, Object... message)   {
    assertTrue(conditionTrue, message);
  }*/

  /**
   * Asserts that a condition is true.
   * 
   * @param condition
   *          tested condition
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   */
  //public static void //
  //    assertTrue(boolean condition, Object... message)   {
  //  if (!condition)
  //    fail(message);
  //}

  /**
   * Asserts that a condition is false.
   * 
   * @param condition
   *          tested condition
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   */
  // public static void //
  //     assertFalse(boolean condition, Object... message)   {
  //   assertTrue(!condition, message);
  // }

  /**
   * Asserts that two objects are equal. When instances of Comparable, such as
   * String, compareTo is used.
   * 
   * @param first
   *          first object
   * @param second
   *          second object
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   * @ 
   *           when objects are not equal with message provided
   */
  @SuppressWarnings("unchecked")
  public static <T> void //
      assertEquals(T first, T second, Object... message)   {
    if (first == null && second == null)
      return;
    assertFalse(first == null && second != null, message.toString());
    assertFalse(first != null && second == null, message.toString());
    if (first instanceof Comparable)
      assertTrue(((Comparable<T>) first).compareTo(second) == 0, message.toString());
    else
      assertTrue(first.equals(second), message.toString());
  }

  public static <T> void //
      assertEqualsInt(int first, int second, Object... message)   {
    assertTrue(first == second, message.toString());
  }

  public static <T> void //
      assertEqualsLong(long first, long second, Object... message)
            {
    assertTrue(first == second, message.toString());
  }

  public static <T> void //
      assertEqualsBool(boolean first, boolean second, Object... message)
            {
    assertTrue(first == second, message.toString());
  }

  /**
   * Asserts that an object is null.
   * 
   * @param object
   *          Assert that object is not null
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   */
  public static void //
      assertNull(Object object, Object... message) {
    assertTrue(object == null, message.toString());
  }

  /**
   * Asserts that an object is not null.
   * 
   * @param object
   *          Assert that object is not null
   * @param message
   *          a space separated message[i].toString() compilation for
   *          i=<0,message.length)
   */
  public static void //
      assertNotNull(Object object, Object... message) {
    assertTrue(object != null, message.toString());
  }

  /**
   * Throws Fault with space separated objects[1],object[2],...,object[n]
   * message
   * 
   * @param objects
   *          objects whose toString() results will be added to Fault message
   * @throws Fault
   *           fault with space separated objects.toString values
   */
  public static void fault(Object... objects) throws Fault  {
    throw new Fault(objectsToString(objects));
  }

  /**
   * Assert that given substring is a substring of given string
   * 
   * @param string
   *          the string to search substring in
   * @param substring
   *          the substring to be searched in a given string
   * @param message
   *          space separated message values to be thrown
   * @ 
   *           throws
   */
  public static void assertContains(String string, String substring,
      Object... message)   {
    assertTrue(string.contains(substring), message.toString());
  }

  /**
   * Assert that given substring is a substring of given string, case
   * insensitive
   * 
   * @param string
   *          the string to search substring in
   * @param substring
   *          the substring to be searched in a given string
   * @param message
   *          space separated message values to be thrown
   * @ 
   */
  public static void assertContainsIgnoreCase(String string, String substring,
      Object... message)   {
    assertTrue(string.toLowerCase().contains(substring.toLowerCase()), message.toString());
  }

  /**
   * Assert that given subtext.toString() subject is a substring of given text
   * 
   * @param text
   *          the text.toString() object to search subtext.toString() in
   * @param subtext
   *          the subtext.toString() to be searched in a given text.toString()
   * @param message
   *          space separated message values to be thrown
   * @ 
   */
  public static <T> void assertContains(T text, T subtext, Object... message)
        {
    assertContains(text.toString(), subtext.toString(), message.toString());
  }

  /**
   * Assert that given subtext.toString() subject is a substring of given text,
   * case insensitive
   * 
   * @param text
   *          the text.toString() object to search subtext.toString() in
   * @param subtext
   *          the subtext.toString() to be searched in a given text.toString()
   * @param message
   *          space separated message values to be thrown
   * @ 
   */
  public static <T> void assertContainsIgnoreCase(T text, T subtext,
      Object... message)   {
    assertContainsIgnoreCase(text.toString(), subtext.toString(), message.toString());
  }

  /**
   * Searches an encapsulated exception cause in parent exception
   */
  protected static <T extends Throwable> T assertCause(Throwable parent,
      Class<T> wrapped, Object... msg)   {
    T t = hasCause(parent, wrapped);
    assertNotNull(t, msg);
    return t;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T //
      hasCause(Throwable parent, Class<? extends Throwable> cause) {
    while (parent != null) {
      if (cause.isInstance(parent))
        return (T) parent;
      parent = parent.getCause();
    }
    return null;
  }

  public static String getStatusCode(Response.Status status) {
    return String.valueOf(status.getStatusCode());
  }

  public static void logMsg(Object... msg) {
    TestUtil.logMsg(objectsToString(msg));
  }

  public static void logTrace(Object... msg) {
    TestUtil.logTrace(objectsToString(msg));
  }

  /**
   * Use rather this method than
   * {@link JaxrsUtil#iterableToString(String, Iterable)} since not all wars
   * (for servlet vehicle, api) do contain JaxrsUtil
   * 
   * @param objects
   * @return objects in a single string , each object separated by " "
   */
  protected static String objectsToString(Object... objects) {
    return objectsToString(" ", objects);
  }

  /**
   * @since 2.0.1
   */
  protected static String objectsToString(String delimiter, Object... objects) {
    StringBuilder sb = new StringBuilder();
    for (Object o : objects)
      sb.append(o).append(delimiter);
    return sb.toString().trim();
  }

  /*
   * private methods
   * ========================================================================
   */
  private String getTSRequest(String request) {
    TestUtil.logTrace("[JAXRSCommonClient] getTSRequest");
    StringBuffer finReq = new StringBuffer(50);
    finReq.append(GET).append(_contextRoot).append(SL).append(_generalURI);
    finReq.append(SL).append(request).append(HTTP11);
    return finReq.toString();
  }

  /**
   * Clears the contents of TEST_PROPS
   */
  protected void clearTestProperties() {
    TEST_PROPS.clear();
  }

  protected boolean isNullOrEmpty(String val) {
    return val == null || val.trim().equals("");
  }

  private InetAddress[] _addrs = null;

  protected String _servlet = null;

  /**
   * Sets the request, testname, and a search string for test passed. A search
   * is also added for test failure. If found, the test will fail.
   * 
   * @param testValue
   *          - a logical test identifier
   * @param testCase
   *          - the current test case
   */
  private void setApiTestProperties(String testValue, WebTestCase testCase) {
    TestUtil.logTrace("[JAXRSCommonClient] setApiTestProperties");

    if (testValue == null) {
      return;
    }

    // An API test consists of a request with a request parameter of
    // testname, a search string of Test PASSED, and a logical test name.

    // set the testname
    _testName = testValue;

    // set the request
    StringBuffer sb = new StringBuffer(50);
    if ((_servlet != null)
        && (TEST_PROPS.get(Property.DONOTUSEServletName) == null)) {
      sb.append(GET).append(_contextRoot).append(SL);
      sb.append(_servlet).append("?testname=").append(testValue);
      sb.append(HTTP11);
    } else {
      sb.append(GET).append(_contextRoot).append(SL);
      sb.append(testValue).append(HTTP10);
    }
    System.out.println("REQUEST LINE: " + sb.toString());

    HttpRequest req = createHttpRequest(sb.toString(), _hostname, _port);
    testCase.setRequest(req);

    String value = TEST_PROPS.get(Property.SEARCH_STRING);
    if (isNullOrEmpty(value)) {
      testCase.setResponseSearchString(Data.PASSED);
      testCase.setUnexpectedResponseSearchString(Data.FAILED);
    }
  }

  protected HttpRequest createHttpRequest(String requestLine, String host,
      int port) {
    return new HttpRequest(requestLine, host, port);
  }

  /**
   * Consists of a test name, a request, and a goldenfile.
   * 
   * @param testValue
   *          - a logical test identifier
   * @param testCase
   *          - the current test case
   */
  private void setStandardProperties(String testValue, WebTestCase testCase) {
    TestUtil.logTrace("[JAXRSCommonClient] setStandardProperties");

    if (testValue == null) {
      return;
    }
    // A standard test sets consists of a testname
    // a request, and a goldenfile. The URI is not used
    // in this case since the JSP's are assumed to be located
    // at the top of the contextRoot
    String req;

    // set the testname
    _testName = testValue;

    if (_servlet != null) {
      req = buildRequest(Request.GET, _servlet, "?testname=", testValue);
    } else {
      req = buildRequest10(Request.GET, testValue);
    }
    System.out.println("REQUEST LINE: " + req);
    System.out.println("_hostname=" + _hostname);
    HttpRequest httpReq = createHttpRequest(req, _hostname, _port);
    testCase.setRequest(httpReq);

    // set the goldenfile
    StringBuffer sb = new StringBuffer(50);
    sb.append(_tsHome).append(GOLDENFILEDIR);
    sb.append(_generalURI).append(SL);
    sb.append(testValue).append(GF_SUFFIX);
    testCase.setGoldenFilePath(sb.toString());
  }

  /**
   * Sets the name of the servlet to use when building a request for a single
   * servlet API test.
   * 
   * @param servlet
   *          - the name of the servlet
   */
  protected void setServletName(String servlet) {
    TestUtil.logTrace("[JAXRSCommonClient] setServletName");
    _servlet = servlet;
  }

  protected String getServletName() {
    return _servlet;
  }

  protected String getLocalInterfaceInfo(boolean returnAddresses) {
    String result = null;
    initInetAddress();
    if (_addrs.length != 0) {
      StringBuffer sb = new StringBuffer(32);
      if (!returnAddresses) {
        // localhost might not show up if aliased
        sb.append("localhost,");
      } else {
        // add 127.0.0.1
        sb.append("127.0.0.1,");
      }

      for (int i = 0; i < _addrs.length; i++) {
        if (returnAddresses) {
          String ip = _addrs[i].getHostAddress();
          if (!ip.equals("127.0.0.1")) {
            if (ip.contains("%")) {
              int scope_id = ip.indexOf("%");
              ip = ip.substring(0, scope_id);
            }
            sb.append(ip);
          }
        } else {
          String host = _addrs[i].getCanonicalHostName();
          if (!host.equals("localhost")) {
            sb.append(host);
          }
        }
        if (i + 1 != _addrs.length) {
          sb.append(",");
        }
      }
      result = sb.toString();
      TestUtil.logTrace("[AbstractUrlClient] Interface info: " + result);
    }
    return result;
  }

  private void initInetAddress() {
    if (_addrs == null) {
      try {
        _addrs = InetAddress
            .getAllByName(InetAddress.getLocalHost().getCanonicalHostName());
      } catch (UnknownHostException uhe) {
        TestUtil.logMsg(
            "[AbstractUrlClient][WARNING] Unable to obtain local host information.");
      }
    }
  }

  protected String getAbsoluteUrl() {
    return getAbsoluteUrl(null);
  }

  protected String getAbsoluteUrl(String method) {
    StringBuilder sb = new StringBuilder();
    sb.append("http://").append(_hostname).append(":").append(_port)
        .append(getContextRoot());
    if (method != null)
      sb.append("/").append(method);
    return sb.toString();
  }

  /**
   * This exception must be thrown to signify a
   * test failure. Overrides 3 printStackTrace methods to preserve the original
   * stack trace.
   *
   * @author Kyle Grucci
   */

  public static class Fault extends Exception {
    private static final long serialVersionUID = -1574745208867827913L;

    public Throwable t;

    /**
     * creates a Fault with a message
     */
    public Fault(String msg) {
      super(msg);
      TestUtil.logErr(msg);
    }

    /**
     * creates a Fault with a message.
     *
     * @param msg
     *          the message
     * @param t
     *          prints this exception's stacktrace
     */
    public Fault(String msg, Throwable t) {
      super(msg);
      this.t = t;
      TestUtil.logErr(msg, t);
    }

    /**
     * creates a Fault with a Throwable.
     *
     * @param t
     *          the Throwable
     */
    public Fault(Throwable t) {
      super(t);
      this.t = t;
    }

    /**
     * Prints this Throwable and its backtrace to the standard error stream.
     *
     */
    public void printStackTrace() {
      if (this.t != null) {
        this.t.printStackTrace();
      } else {
        super.printStackTrace();
      }
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param s
     *          <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream s) {
      if (this.t != null) {
        this.t.printStackTrace(s);
      } else {
        super.printStackTrace(s);
      }
    }

    /**
     * Prints this throwable and its backtrace to the specified print writer.
     *
     * @param s
     *          <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(PrintWriter s) {
      if (this.t != null) {
        this.t.printStackTrace(s);
      } else {
        super.printStackTrace(s);
      }
    }

    @Override
    public Throwable getCause() {
      return t;
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
      if (t != null)
        throw new IllegalStateException("Can't overwrite cause");
      if (!Exception.class.isInstance(cause))
        throw new IllegalArgumentException("Cause not permitted");
      this.t = (Exception) cause;
      return this;
    }
  }

}