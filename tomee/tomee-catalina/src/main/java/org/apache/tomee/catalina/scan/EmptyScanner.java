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
package org.apache.tomee.catalina.scan;

import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;

import jakarta.servlet.ServletContext;

public class EmptyScanner implements JarScanner {
    private JarScanFilter scanner;

    @Override
    public void scan(final JarScanType scanType, final ServletContext context,
                     final JarScannerCallback callback) {
        // no-op
    }

    @Override
    public JarScanFilter getJarScanFilter() {
        return scanner;
    }

    @Override
    public void setJarScanFilter(final JarScanFilter jarScanFilter) {
        this.scanner = jarScanFilter;
    }
}
