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
import java.util.LinkedList;
import java.util.List;

import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import org.apache.commons.httpclient.Header;
import jakarta.ws.rs.core.MultivaluedMap;

public class ApacheResponseAdapter extends HttpResponse {

  public ApacheResponseAdapter(jakarta.ws.rs.core.Response response, String host,
      int port) {
    super(host, port, port == 443, null, null);
    this.response = response;
    this.caser = TextCaser.NONE;
  }

  public ApacheResponseAdapter(jakarta.ws.rs.core.Response response, String host,
      int port, TextCaser caser) {
    this(response, host, port);
    this.caser = caser;
  }

  private jakarta.ws.rs.core.Response response;

  private String entity = null;

  private TextCaser caser = null;

  /**
   * Returns the HTTP status code returned by the server
   * 
   * @return HTTP status code
   */
  public String getStatusCode() {
    return Integer.toString(response.getStatus());
  }

  @Override
  public String getResponseBodyAsString() throws IOException {
    if (entity == null)
      entity = response.readEntity(String.class);
    return entity == null ? "" : caser.getCasedText(entity);
  }

  @Override
  public String getResponseBodyAsRawString() throws IOException {
    return getResponseBodyAsString();
  }

  @Override
  public String getReasonPhrase() {
    return response.toString();// getReasonPhrase();
  }

  @Override
  public Header[] getResponseHeaders() {
    List<Header> headers = new LinkedList<Header>();
    MultivaluedMap<String, Object> mHeaders = response.getMetadata();
    String[] sHeaders = JaxrsCommonClient.getMetadata(mHeaders);
    for (String header : sHeaders) {
      String[] split = header.split(":", 2);
      headers.add(new Header(split[0], split[1]));
    }
    return headers.toArray(new Header[headers.size()]);
  }

  @Override
  public Header getResponseHeader(String headerName) {
    for (Header header : getResponseHeaders())
      if (header.getName().equals(headerName))
        return header;
    return null;
  }

  @Override
  public String getResponseEncoding() {
    String encoding = null;
    Header header = getResponseHeader("Content-Type");
    if (header != null) {
      String headerVal = header.getValue();
      int idx = headerVal.indexOf(";charset=");
      if (idx > -1) {
        // content encoding included in response
        encoding = headerVal.substring(idx + 9);
      }
    }
    return encoding;
  }
}
