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
package org.apache.tomee.catalina;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.ContextName;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

public class Contexts {

    public static String toAppContext(final ServletContext servletContext, final String contextPath) {
        return servletContext.getVirtualServerName() + " " + contextPath;
    }

    public static String toAppContext(final StandardContext standardContext) {
        return toAppContext(standardContext.getServletContext(), standardContext.getPath());
    }

    public static String getHostname(final StandardContext ctx) {
        String hostName = null;
        final Container parentHost = ctx.getParent();
        if (parentHost != null) {
            hostName = parentHost.getName();
        }
        if ((hostName == null) || (hostName.length() < 1)) {
            hostName = "_";
        }
        return hostName;
    }

    public static File warPath(final Context standardContext) {
        final File file = realWarPath(standardContext);
        if (file == null) {
            return null;
        }

        final String name = file.getName();
        if (!file.isDirectory() && name.endsWith(".war")) {
            final File extracted = new File(file.getParentFile(), name.substring(0, name.length() - ".war".length()));
            if (extracted.exists()) {
                try {
                    return extracted.getCanonicalFile();
                } catch (final IOException e) {
                    return extracted;
                }
            }
        }
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    public static File realWarPath(final Context standardContext) {
        if (standardContext == null) {
            return null;
        }

        final File docBase;
        Container container = standardContext;
        while (container != null) {
            if (container instanceof Host) {
                break;
            }
            container = container.getParent();
        }

        String baseName = null;
        if (standardContext.getDocBase() != null) {
            File file = new File(standardContext.getDocBase());
            if (!file.isAbsolute()) {
                if (container == null) {
                    docBase = new File(engineBase(standardContext), standardContext.getDocBase());
                } else {
                    final String appBase = ((Host) container).getAppBase();
                    file = new File(appBase);
                    if (!file.isAbsolute()) {
                        file = new File(engineBase(standardContext), appBase);
                    }
                    docBase = new File(file, standardContext.getDocBase());
                }
            } else {
                docBase = file;
            }
        } else {
            final String path = standardContext.getPath();
            if (path == null) {
                throw new IllegalStateException("Can't find docBase");
            } else {
                baseName = new ContextName(path, standardContext.getWebappVersion()).getBaseName();
                docBase = new File(baseName);
            }
        }

        if (!docBase.exists() && baseName != null) { // for old compatibility, will be removed soon
            if (Host.class.isInstance(container)) {
                final File file = new File(Host.class.cast(container).getAppBaseFile(), baseName);
                if (file.exists()) {
                    return file;
                }
            }
            return oldRealWarPath(standardContext);
        }

        final String name = docBase.getName();
        if (name.endsWith(".war")) {
            final File extracted = new File(docBase.getParentFile(), name.substring(0, name.length() - ".war".length()));
            if (extracted.exists()) {
                return extracted;
            }
        }

        return docBase;
    }

    private static File engineBase(final Context standardContext) {
        final String base = System.getProperty(Globals.CATALINA_BASE_PROP);
        if( base == null ) {
            final StandardEngine eng = (StandardEngine) standardContext.getParent().getParent();
            return eng.getCatalinaBase();
        }
        return new File(base);
    }

    @Deprecated
    private static File oldRealWarPath(final Context standardContext) {
        String doc = standardContext.getDocBase();
        // handle ROOT case
        if (doc == null || doc.length() == 0) {
            doc = "ROOT";
        }

        File war = new File(doc);
        if (war.exists()) {
            return war;
        }

        final StandardHost host = (StandardHost) standardContext.getParent();
        final String base = host.getAppBase();
        war = new File(base, doc);
        if (war.exists()) {
            return war;
        }

        war = new File(new File(System.getProperty("catalina.home"), base), doc);
        if (war.exists()) {
            return war;
        }
        return new File(new File(System.getProperty("catalina.base"), base), doc); // shouldn't occur
    }
}
