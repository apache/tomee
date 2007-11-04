/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import java.io.IOException;

/**This interface takes care of HTTP Responses.  It sends data back to the browser.
 * 
 */
public interface HttpResponse extends java.io.Serializable{
    /** sets a header to be sent back to the browser
     * @param name the name of the header
     * @param value the value of the header
     */
    public void setHeader(String name, String value);

    /** Gets a header based on the name passed in
     * @param name The name of the header
     * @return the value of the header
     */
    public String getHeader(String name);

    /** Gets the PrintWriter to send data to the browser
     * @return the PrintWriter to send data to the browser
     */
    public java.io.PrintWriter getPrintWriter() throws IOException;

    /** gets the OutputStream to send data to the browser
     * @return the OutputStream to send data to the browser
     */
    public java.io.OutputStream getOutputStream();

    /** sets the content type to be sent back to the browser
     * @param type the type to be sent to the browser (i.e. "text/html")
     */
    public void setContentType(String type);

    /** gets the content type that will be sent to the browser
     * @return the content type (i.e. "text/html")
     */
    public String getContentType();

    /** sets the HTTP response code to be sent to the browser.  These codes are:
     *
     * OPTIONS = 0
     * GET     = 1
     * HEAD    = 2
     * POST    = 3
     * PUT     = 4
     * DELETE  = 5
     * TRACE   = 6
     * CONNECT = 7
     * UNSUPPORTED = 8
     * @param code the code to be sent to the browser
     */
    void setStatusCode(int code);

    /** gets the HTTP response code
     * @return the HTTP response code
     */
    int getStatusCode();

    /** Sets the response string to be sent to the browser
     * @param responseString the response string
     */
    void setStatusMessage(String responseString);

    void flushBuffer() throws IOException;
}
