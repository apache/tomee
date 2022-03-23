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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * This interface takes care of HTTP Responses.  It sends data back to the browser.
 */
public interface HttpResponse extends java.io.Serializable, HttpServletResponse {
    /**
     * Gets the content type that will be sent to the browser.
     *
     * @return the content type (i.e. "text/html")
     */
    public String getContentType();

    /**
     * Sets the content type to be sent back to the browser.
     *
     * @param type the type to be sent to the browser (i.e. "text/html")
     */
    public void setContentType(String type);

    /**
     * Sets the response string to be sent to the browser
     *
     * @param responseString the response string
     */
    void setStatusMessage(String responseString);

    /**
     * Flushes the output buffer to the client.
     */
    void flushBuffer() throws IOException;
}
