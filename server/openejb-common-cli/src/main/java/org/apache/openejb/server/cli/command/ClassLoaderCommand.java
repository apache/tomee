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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cli.command;

import org.apache.openejb.AppContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.xbean.finder.UrlSet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Command(name = ClassLoaderCommand.CLASSLOADER_CMD, usage = ClassLoaderCommand.CLASSLOADER_CMD + " <app>", description = "print classloader info")
public class ClassLoaderCommand extends AbstractCommand {
    public static final String CLASSLOADER_CMD = "classloader";
    private static final String INDENT = "  ";

    @Override
    public void execute(final String cmd) {
        final String appName = extractAppName(cmd);
        final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        for (AppContext ctx : cs.getAppContexts()) {
            if (appName.equalsIgnoreCase(ctx.getId())) {
                dumpClassLoader(ctx.getClassLoader());
                return;
            }
        }
        streamManager.writeErr("can't find app " + appName);
        streamManager.writeErr("available apps are:");
        for (AppContext ctx : cs.getAppContexts()) {
            streamManager.writeErr("- " + ctx.getId());
        }
    }

    protected void dumpClassLoader(final ClassLoader classLoader) {
        final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        ClassLoader current = classLoader;
        while (current != null) {
            classLoaders.add(current);
            current = current.getParent();
        }

        Collections.reverse(classLoaders);

        for (ClassLoader cl : classLoaders) {
            streamManager.writeOut("+" + cl.toString());

            UrlSet urls;
            try {
                urls = new UrlSet(cl);
                if (cl.getParent() != null) {
                    urls = urls.exclude(cl.getParent());
                }
            } catch (IOException e) {
                streamManager.writeErr(INDENT + "` can't get urls of this classloader");
                continue;
            }

            final List<URL> listUrls = urls.getUrls();
            Collections.sort(listUrls, new URLComparator());
            final Iterator<URL> it = listUrls.iterator();
            while (it.hasNext()) {
                final String value = it.next().toExternalForm();
                final StringBuilder builder = new StringBuilder(INDENT);
                if (it.hasNext()) {
                    builder.append('|');
                } else {
                    builder.append('`');
                }
                builder.append(" ").append(value);
                streamManager.writeOut(builder.toString());
            }
        }
    }

    protected String extractAppName(final String cmd) {
        if (CLASSLOADER_CMD.length() == cmd.length()) {
            return "";
        }
        return cmd.substring(CLASSLOADER_CMD.length()).trim();
    }

    private class URLComparator implements Comparator<URL> {
        @Override
        public int compare(final URL o1, URL o2) {
            return o1.toExternalForm().compareTo(o2.toExternalForm());
        }
    }
}
