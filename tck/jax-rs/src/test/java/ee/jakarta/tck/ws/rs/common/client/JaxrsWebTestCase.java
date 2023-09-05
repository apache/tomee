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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import ee.jakarta.tck.ws.rs.common.webclient.TestFailureException;
import ee.jakarta.tck.ws.rs.common.webclient.WebTestCase;
import ee.jakarta.tck.ws.rs.common.webclient.http.HttpRequest;
import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import ee.jakarta.tck.ws.rs.common.webclient.validation.ValidationFactory;
import ee.jakarta.tck.ws.rs.common.webclient.validation.ValidationStrategy;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

/**
 * Replaces WebTestCase to be used with JAXRS client instead of apache client
 * 
 * @author supol
 */
public class JaxrsWebTestCase extends WebTestCase {

  /**
   * The JAXRS request instance
   */
  protected Request request;

  /**
   * The JAXRS response instance
   */
  protected Response response;

  /**
   * GET, PUT, OPTIONS, ...
   */
  protected String requestType;

  /**
   * The URL of the Request
   */
  protected String urlRequest;

  /**
   * The HTTP content entity. A MessageBodyWriter<entity.getClass()> needs to be
   * registered if not a standard entity type supported by JAXRS
   */
  protected Object entity;

  /**
   * port of the server
   */
  protected int port;

  /**
   * host name of the server
   */
  protected String hostname;

  /**
   * HTTP header list
   */
  protected Map<String, String> headerMap;

  /**
   * Apache HTTP response mock
   */
  protected HttpResponse _response;

  /**
   * print the response entity
   */
  protected boolean printEntity = true;

  /**
   * buffer the returned entity
   */
  protected boolean bufferEntity = false;

  /**
   * Provider logging the request and response
   */
  protected Object loggingFilter;

  /**
   * other providers, such as MessageBodyReader, or MessageBodyWriter
   */
  protected List<Object> providersToRegister;

  /**
   * Strategy to use when validating the test case against the server's
   * response.
   */
  protected ValidationStrategy strategy = null;

  /**
   * Show the call client code, used to be printed when report issues
   */
  protected boolean printClientCall = false;

  /**
   * Upper case, lower case, or exact text matching?
   */
  protected TextCaser textCaser = TextCaser.NONE;

  /**
   * Type of execution
   */
  protected enum Execution {
    SYNCHRONOUS, ASYNCHRONOUS
  }

  /**
   * Runnable to run while asynchronous
   */
  protected Runnable asyncRunnable;

  /**
   * Execution type instance
   */
  protected Execution executionType = Execution.SYNCHRONOUS;

  /**
   * Client instance here not to be garbage collected before end of test.
   */
  protected Client client;

  public JaxrsWebTestCase() {
    strategy = ValidationFactory.getInstance(TOKENIZED_STRATEGY);
    headerMap = new HashMap<String, String>();
    providersToRegister = new LinkedList<Object>();
    closeClient();
  }

  /**
   * Sets the validation strategy for this test case instance.
   * 
   * @param validator
   *          - the fully qualified class name of the response validator to use.
   */
  public void setStrategy(String validator) {
    ValidationStrategy strat = ValidationFactory.getInstance(validator);
    if (strat != null) {
      strategy = strat;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("[WebTestCase][WARNING] An attempt was made to use a ");
      sb.append("non-existing validator (");
      sb.append(validator);
      sb.append(").  Falling back to the TokenizedValidator");
      TestUtil.logMsg(sb.toString());
    }
  }

  /**
   * Executes the test case.
   * 
   * @throws TestFailureException
   *           if the test fails for any reason.
   * @throws IllegalStateException
   *           if no request was configured or if no Validator is available at
   *           runtime.
   */
  public void execute() throws TestFailureException {
    verifyValidationStrategy();
    verifySettings();
    try {
      String url = logClientRequestAndGetUrl();

      client = getClientWithRegisteredProviders();
      WebTarget target = client.target(url.toString());
      Invocation i = buildRequest(target);
      response = invoke(i);
      if (bufferEntity)
        response.bufferEntity();
    } catch (Throwable t) {
      String message = t.getMessage();

      StringBuilder sb = new StringBuilder();
      sb.append("[FATAL] Unexpected failure during test execution.\n");
      // print client call code to report into JIRA when needed
      sb.append(printClientCall().toString());
      // Inherited message
      sb.append((message == null ? t.toString() : message));

      throw new TestFailureException(sb.toString(), t);
    }

    // Validate this test case instance
    if (!strategy.validate(this)) {
      throw new TestFailureException("Test FAILED!");
    }
  }

  public void closeClient() {
    if (client != null)
      client.close();
    client = null;
  }

  protected void verifyValidationStrategy() {
    // If no strategy instance is available (strange, but has happened),
    // fail.
    try {
      getStrategy();
    } catch (NullPointerException e) {
      throw new IllegalStateException("[FATAL] No Validator available.");
    }
  }

  protected void verifySettings() throws TestFailureException {
    if (hostname == null)
      throw new TestFailureException("No hostname set");
    if (port == 0)
      throw new TestFailureException("Port not set");
    if (requestType == null)
      throw new TestFailureException("No request method set");
    if (urlRequest == null)
      throw new TestFailureException("No resource url request set");
  }

  /**
   * @return Client with all providers already registered
   */
  protected Client getClientWithRegisteredProviders() {
    Client client = ClientBuilder.newClient();
    client.register(new JdkLoggingFilter(isPrintedEntity()));
    for (Object o : providersToRegister)
      if (o instanceof Class)
        client.register((Class<?>) o); // otherwise it does not work
      else
        client.register(o);
    return client;
  }

  protected String logClientRequestAndGetUrl() {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(hostname).append(":").append(port);
    url.append(urlRequest);

    StringBuilder msg = new StringBuilder();
    msg.append("[Request] Dispatching request: '").append(requestType)
        .append(" ").append(url).append("' to target server at '")
        .append(hostname).append(":").append(port).append("'");
    TestUtil.logMsg(msg.toString());
    TestUtil.logMsg("###############################");

    if (printClientCall)
      TestUtil.logMsg(printClientCall().toString());

    return url.toString();
  }

  /**
   * Log java code executed
   */
  protected StringBuilder printClientCall() {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(hostname).append(":").append(port);
    url.append(urlRequest);

    StringBuilder sb = new StringBuilder();
    // print client call code to report into JIRA when needed
    sb.append("Client client = ClientFactory.newClient();\n");
    for (Object o : providersToRegister) {
      sb.append("client.configuration().register(");
      if (o instanceof Class)
        sb.append(((Class<?>) o).getName()).append(".class");
      else
        sb.append(o.getClass().getName());
      sb.append(");\n");
    }
    sb.append("WebTarget target = client.target(\"").append(url.toString())
        .append("\");\n");
    sb.append("Invocation.Builder builder;\n");
    sb.append("builder = target.request(\"").append(getAcceptMediaType())
        .append("\");\n");
    for (Entry<String, String> entry : headerMap.entrySet()) {
      sb.append("builder.header(\"").append(entry.getKey()).append("\",\"")
          .append(entry.getValue()).append("\");\n");
    }
    sb.append("Invocation i;\n");
    sb.append("i=builder.build(\"").append(requestType).append("\"");
    if (entity != null)
      sb.append(",").append("Entity.entity(").append(entity.toString())
          .append(",").append(getContentType());
    sb.append(");\n");
    sb.append("i.invoke();\n");

    return sb;
  }

  /**
   * Build Invocation
   */
  protected Invocation buildRequest(WebTarget target) {
    Invocation.Builder builder;
    builder = target.request(getAcceptMediaType());
    for (Entry<String, String> entry : headerMap.entrySet()) {
      if (!entry.getKey().equals("Accept"))
        builder.header(entry.getKey(), entry.getValue());
    }
    Invocation i;
    if (entity != null) {
      if (entity instanceof Entity)
        i = builder.build(requestType, (Entity<?>) entity);
      else
        i = builder.build(requestType, Entity.entity(entity, getContentType()));
      TestUtil.logMsg("[Request] Adding entity: " + entity);
    } else
      i = builder.build(requestType);
    return i;
  }

  /**
   * Invoke the invocation synchronously, or asynchronously
   */
  protected Response invoke(Invocation invocation) throws TestFailureException {
    Response response = null;
    switch (executionType) {
    case SYNCHRONOUS:
      response = invocation.invoke();
      break;
    case ASYNCHRONOUS:
      int cnt = 0;
      try {
        final boolean[] buffered = { false };
        InvocationCallback<Response> callback = new InvocationCallback<Response>() {
          @Override
          public void completed(Response res) {
            try {
              JaxrsWebTestCase.this.response = res;
              // buffer before stream is closed
              getResponse().getResponseBodyAsString();
              buffered[0] = true;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          @Override
          public void failed(Throwable throwable) {
            throw new RuntimeException(throwable);
          }
        };
        Future<Response> future = invocation.submit(callback);
        while (!buffered[0] && cnt++ < 50) {
          Thread.sleep(100L);
        }
        response = future.get();
        // response = invocation.submit().get();
      } catch (Exception e) {
        throw new TestFailureException(e);
      }
      if (cnt > 49) {
        throw new TestFailureException(
            "Invocation callback has not been called within 5 second");
      }
    }
    return response;
  }

  /**
   * Get media type in Request Content Type
   */
  protected String getAcceptMediaType() {
    String media = headerMap.get("Accept");
    if (media == null)
      media = MediaType.WILDCARD;
    return media;
  }

  /**
   * Get media type in Request Content Type
   */
  protected String getContentType() {
    String media = headerMap.get("Content-Type");
    if (media == null)
      media = MediaType.WILDCARD;
    return media;
  }

  // ---------------------------------------------------------------------
  // Apache adaptor methods

  @Override
  public HttpRequest getRequest() {
    if (super.getRequest() == null) {
      StringBuilder sb = new StringBuilder();
      sb.append(requestType).append(" ").append(urlRequest).append("/");
      sb.append(" HTTP/1.1");
      super.setRequest(new ApacheRequestAdapter(sb.toString(), hostname, port));
    }
    return super.getRequest();
  }

  @Override
  public synchronized HttpResponse getResponse() {
    if (_response == null) {
      _response = new ApacheResponseAdapter(response, hostname, port,
          textCaser);
    }
    return _response;
  }

  // ---------------------------------------------------------------------
  // Getters and Setters

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getUrlRequest() {
    return urlRequest;
  }

  public void setUrlRequest(String urlRequest) {
    this.urlRequest = urlRequest;
  }

  public void addHeader(String name, String value) {
    headerMap.put(name, value);
  }

  public Response getJaxrsResponse() {
    return response;
  }

  public void setEntity(Object entity) {
    this.entity = entity;
  }

  /**
   * Returns the Request for this particular test case.
   * 
   * @return Request of this test case
   */
  public Request getJaxrsRequest() {
    return request;
  }

  public boolean isPrintedEntity() {
    return printEntity;
  }

  /**
   * Set whether the entity is to be printed in trace log or not;
   */
  public void setPrintEntity(boolean printEntity) {
    this.printEntity = printEntity;
  }

  public void bufferEntity(boolean bufferEntity) {
    this.bufferEntity = bufferEntity;
  }

  public List<Object> getProvidersToRegister() {
    return providersToRegister;
  }

  public void addProviderToRegister(Object providerToRegister) {
    this.providersToRegister.add(providerToRegister);
  }

  public void setPrintClientCall(boolean print) {
    printClientCall = print;
  }

  public void setProcessingType(Execution type) {
    executionType = type;
  }

  public TextCaser getTextCaser() {
    return textCaser;
  }

  public void setTextCaser(TextCaser textCaser) {
    this.textCaser = textCaser;
  }

}
