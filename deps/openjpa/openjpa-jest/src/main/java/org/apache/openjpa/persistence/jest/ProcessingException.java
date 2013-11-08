/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.openjpa.persistence.jest;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.apache.openjpa.persistence.jest.Constants.MIME_TYPE_XML;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.openjpa.lib.util.Localizer.Message;
import org.w3c.dom.Document;

/**
 * Specialized RuntimException thrown by JEST commands.
 * The exception can be serialized to the output stream of a HTTP Servlet response as a HTML page.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class ProcessingException extends RuntimeException {
    private final JPAServletContext ctx;
    private final int _errorCode;
    
    public ProcessingException(JPAServletContext ctx, Throwable error) {
        this(ctx, error, HTTP_INTERNAL_ERROR);
    }
    
    public ProcessingException(JPAServletContext ctx, Throwable error, int errorCode) {
        super(error);
        this.ctx = ctx;
        this._errorCode = errorCode;
    }

    public ProcessingException(JPAServletContext ctx, Message message, int errorCode) {
        super(message.toString());
        this.ctx = ctx;
        this._errorCode = errorCode;
    }
    
    public ProcessingException(JPAServletContext ctx, Throwable error, Message message) {
        this(ctx, error, message, HTTP_INTERNAL_ERROR);
    }
    
    public ProcessingException(JPAServletContext ctx, Throwable error, Message message, int errorCode) {
        super(message.toString(), error);
        this.ctx = ctx;
        this._errorCode = errorCode;
    }
    
    /**
     * Prints the stack trace in a HTML format on the given response output stream.
     * 
     * @param response
     * @throws IOException
     */
    public void printStackTrace() {
        HttpServletResponse response = ctx.getResponse();
        response.setContentType(MIME_TYPE_XML);
        response.setStatus(_errorCode);

        String uri = ctx.getRequestURI().toString();
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (Exception e) {
        }
        Throwable t = this.getCause() == null ? this : getCause();
        ExceptionFormatter formatter = new ExceptionFormatter();
        Document xml = formatter.createXML("Request URI: " + uri, t);
        try {
            formatter.write(xml, response.getOutputStream());
            response.sendError(_errorCode);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Request URI: " + uri, e);
        }
    }
}
