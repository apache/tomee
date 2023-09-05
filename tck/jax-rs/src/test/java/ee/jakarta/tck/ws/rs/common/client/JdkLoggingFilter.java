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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Integer.MAX_VALUE)
public class JdkLoggingFilter extends Formatter
    implements ClientRequestFilter, ClientResponseFilter {

  private static final Logger LOGGER = Logger
      .getLogger(JdkLoggingFilter.class.getName());

  private static final String REQUEST_PREFIX = ">> ";

  private static final String RESPONSE_PREFIX = "<< ";

  //
  private final DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

  private final Logger logger;

  private boolean printEntity = true;

  /**
   * Create a logging filter logging the request and response to a default JDK
   * logger, named as the fully qualified class name of this class.
   */
  public JdkLoggingFilter(boolean printEntity) {
    this(LOGGER, printEntity);
  }

  /**
   * Create a logging filter with custom logger and custom settings of entity
   * logging.
   * 
   * @param logger
   *          the logger to log requests and responses.
   * @param printEntity
   *          if true, entity will be logged as well.
   */
  public JdkLoggingFilter(Logger logger, boolean printEntity) {
    this.logger = logger;
    this.printEntity = printEntity;

    ConsoleHandler handler = new ConsoleHandler();
    logger.setUseParentHandlers(false);
    handler.setFormatter(this);

    // Set handler only once
    if (logger.getHandlers().length == 0)
      logger.addHandler(handler);
  }

  @Override
  public void filter(ClientRequestContext arg0,
      ClientResponseContext responseContext) throws IOException {
    printResponseLine(responseContext.getStatus());
    printPrefixedHeaders(RESPONSE_PREFIX, responseContext.getHeaders());
    if (printEntity && responseContext.hasEntity()) {
      List<String> entity = replaceEntityStream(responseContext);
      log(RESPONSE_PREFIX, entity);
    }

  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    printRequestLine(requestContext.getMethod(), requestContext.getUri());
    MultivaluedMap<String, Object> headers = requestContext.getHeaders();
    StringBuilder sb = new StringBuilder().append(REQUEST_PREFIX);
    for (String header : JaxrsCommonClient.getMetadata(headers)) {
      if (sb.length() > REQUEST_PREFIX.length())
        sb.append(", ");
      sb.append(header);
    }
    log(sb);
    if (printEntity && requestContext.hasEntity()) {
      log(new StringBuilder().append(REQUEST_PREFIX)
          .append(requestContext.getEntity().toString()));
    }
  }

  private static List<String> replaceEntityStream(ClientResponseContext ctx)
      throws IOException {
    List<String> entity = null;
    if (ctx.hasEntity()) {
      InputStream is = ctx.getEntityStream();
      entity = readEntityFromStream(is);
      ByteArrayInputStream bais = new ByteArrayInputStream(
          linesToBytes(entity));
      ctx.setEntityStream(bais);
    }
    return entity;
  }

  private static byte[] linesToBytes(List<String> lines) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> i = lines.iterator(); i.hasNext();) {
      sb.append(i.next());
      if (i.hasNext())
        sb.append("\n");
    }
    return sb.toString().getBytes();
  }

  private static List<String> readEntityFromStream(InputStream is)
      throws IOException {
    String entity;
    List<String> lines = new LinkedList<String>();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    while ((entity = br.readLine()) != null)
      lines.add(entity);
    return lines;
  }

  private void log(StringBuilder b) {
    if (logger != null)
      logger.info(b.append("\n").toString());
  }

  private void log(String prefix, List<String> rows) {
    if (logger != null && rows != null)
      for (String row : rows)
        logger.info(new StringBuilder().append(prefix).append(row).append("\n")
            .toString());
  }

  private void printRequestLine(String method, URI uri) {
    StringBuilder sb = new StringBuilder();
    sb.append(REQUEST_PREFIX).append(method).append(" ")
        .append(uri.toASCIIString());
    log(sb);
  }

  private void printResponseLine(int status) {
    StringBuilder sb = new StringBuilder();
    sb.append(RESPONSE_PREFIX).append(status).append(" ")
        .append(Response.Status.fromStatusCode(status).name());
    log(sb);
  }

  private void printPrefixedHeaders(final String prefix,
      Map<String, List<String>> headers) {
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      StringBuilder sb = new StringBuilder();
      List<String> val = e.getValue();
      String header = e.getKey();

      if (val.size() == 1) {
        sb.append(prefix).append(header).append(": ").append(val.get(0));
      } else {
        StringBuilder sb2 = new StringBuilder();
        for (String s : val) {
          if (sb2.length() != 0)
            sb2.append(',');
          sb2.append(s);
        }
        sb.append(prefix).append(header).append(": ").append(sb2.toString());
      }
      if (sb.length() != 0)
        log(sb);
    }

  }

  @Override
  public String format(LogRecord record) {
    StringBuilder sb = new StringBuilder();
    sb.append(df.format(new Date(record.getMillis()))).append(":  ")
        .append("TRACE: [WIRE] - ");
    sb.append(formatMessage(record));
    return sb.toString();
  }

}
