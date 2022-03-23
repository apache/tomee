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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.httpd.part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpRequestImpl;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

// [fileupload] is optional so using a class to lazy load the deps
public final class CommonsFileUploadPartFactory {
    private static final File REPO;

    static { // TODO: move this config in http service if this code is useful
        String repo = SystemInstance.get().getProperty("tomee.embedded.http.file.repository");
        if (repo == null) {
            for (final String potential : asList("work", "temp", "target", "build")) {
                try {
                    final File directory = SystemInstance.get().getBase().getDirectory(potential);
                    if (directory.isDirectory()) {
                        repo = directory.getAbsolutePath();
                        break;
                    }
                } catch (IOException e) {
                    // try next
                }
            }
        }
        REPO = new File(repo == null ? "." : repo);
    }

    private CommonsFileUploadPartFactory() {
        // no-op
    }

    public static Collection<Part> read(final HttpRequestImpl request) { // mainly for testing
        // Create a new file upload handler
        final DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(REPO);

        final ServletFileUpload upload = new ServletFileUpload();
        upload.setFileItemFactory(factory);

        final List<Part> parts = new ArrayList<>();
        try {
            final List<FileItem> items = upload.parseRequest(new ServletRequestContext(request));
            final String enc = request.getCharacterEncoding();
            for (final FileItem item : items) {
                final CommonsFileUploadPart part = new CommonsFileUploadPart(item, null);
                parts.add(part);
                if (part.getSubmittedFileName() == null) {
                    String name = part.getName();
                    String value = null;
                    try {
                        String encoding = request.getCharacterEncoding();
                        if (encoding == null) {
                            if (enc == null) {
                                encoding = "UTF-8";
                            } else {
                                encoding = enc;
                            }
                        }
                        value = part.getString(encoding);
                    } catch (final UnsupportedEncodingException uee) {
                        try {
                            value = part.getString("UTF-8");
                        } catch (final UnsupportedEncodingException e) {
                            // not possible
                        }
                    }
                    request.addInternalParameter(name, value);
                }
            }

            return parts;
        } catch (final FileUploadException e) {
            throw new IllegalStateException(e);
        }
    }
}
