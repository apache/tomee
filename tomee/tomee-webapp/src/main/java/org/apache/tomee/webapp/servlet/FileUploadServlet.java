/*
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

package org.apache.tomee.webapp.servlet;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.webapp.JsonExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;


public class FileUploadServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, FileUploadServlet.class);

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        JsonExecutor.execute(resp, new JsonExecutor.Executor() {
            @Override
            public void call(Map<String, Object> json) throws Exception {
                final File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");

                final Part filePart = req.getPart("file");
                final String filename = getFileName(filePart);
                final File file = new File(tempDir, filename);
                filePart.write(file.getAbsolutePath());

                json.put("file", file.getAbsolutePath());
            }
        });
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        for (String cd : partHeader.split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
