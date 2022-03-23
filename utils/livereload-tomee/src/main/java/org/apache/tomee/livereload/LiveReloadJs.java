/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import org.apache.openejb.loader.IO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LiveReloadJs extends HttpServlet {
    private byte[] js;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/javascript");
        resp.getOutputStream().write(js);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("js/livereload.js")) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            IO.copy(is, out);
            js = out.toByteArray();
        } catch (final IOException e) {
            throw new IllegalStateException("impossible to find livereload.js", e);
        }
    }
}
