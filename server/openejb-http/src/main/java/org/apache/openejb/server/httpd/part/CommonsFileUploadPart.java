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
package org.apache.openejb.server.httpd.part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

// highly inspired from tomcat
public class CommonsFileUploadPart implements Part {
    private final FileItem fileItem;
    private final File location;

    public CommonsFileUploadPart(final FileItem fileItem, final File location) {
        this.fileItem = fileItem;
        this.location = location;
    }

    @Override
    public void delete() throws IOException {
        fileItem.delete();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getHeader(final String name) {
        return fileItem.getHeaders().getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        if (fileItem instanceof DiskFileItem) {
            final Set<String> headerNames = new LinkedHashSet<>();
            final Iterator<String> iter = fileItem.getHeaders().getHeaderNames();
            while (iter.hasNext()) {
                headerNames.add(iter.next());
            }
            return headerNames;
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getHeaders(final String name) {
        final Set<String> headers = new LinkedHashSet<>();
        final Iterator<String> iter = fileItem.getHeaders().getHeaders(name);
        while (iter.hasNext()) {
            headers.add(iter.next());
        }
        return headers;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public long getSize() {
        return fileItem.getSize();
    }

    @Override
    public void write(final String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.isAbsolute()) {
            file = new File(location, fileName);
        }
        try {
            fileItem.write(file);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String getString(final String encoding) throws UnsupportedEncodingException {
        return fileItem.getString(encoding);
    }

    @Override
    public String getSubmittedFileName() {
        return fileItem.getName();
    }
}
