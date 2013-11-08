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
package org.apache.openjpa.conf;

import java.io.File;
import java.io.InputStream;
import java.security.AccessController;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;

/**
 * This class contains version information for OpenJPA. It uses
 * Ant's filter tokens to convert the template into a java
 * file with current information.
 *
 * @author Marc Prud'hommeaux, Patrick Linskey
 */
public class OpenJPAVersion {

    private static final Localizer _loc = Localizer.forPackage(OpenJPAVersion.class);

    public static final String VERSION_NUMBER;
    public static final String VERSION_ID;
    public static final String VENDOR_NAME = "OpenJPA";
    public static final int MAJOR_RELEASE;
    public static final int MINOR_RELEASE;
    public static final int PATCH_RELEASE;
    public static final String RELEASE_STATUS;
    public static final String REVISION_NUMBER;

    static {
        Properties revisionProps = new Properties();
        try {
            InputStream in = OpenJPAVersion.class.getResourceAsStream
                ("/META-INF/org.apache.openjpa.revision.properties");
            if (in != null) {
                try {
                    revisionProps.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (Exception e) {
        }

        String vers = revisionProps.getProperty("openjpa.version");
        if (vers == null || "".equals(vers.trim()))
            vers = "0.0.0";
        VERSION_NUMBER = vers;

        StringTokenizer tok = new StringTokenizer(VERSION_NUMBER, ".-");
        int major, minor, patch;
        try {
            major = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 0;
        } catch (Exception e) {
            major = 0;
        }

        try {
            minor = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 0;
        } catch (Exception e) {
            minor = 0;
        }

        try {
            patch = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 0;
        } catch (Exception e) {
            patch = 0;
        }

        String revision = revisionProps.getProperty("revision.number");

        MAJOR_RELEASE = major;
        MINOR_RELEASE = minor;
        PATCH_RELEASE = patch;
        RELEASE_STATUS = tok.hasMoreTokens() ? tok.nextToken("!") : "";
        REVISION_NUMBER = revision;
        VERSION_ID = "openjpa-" + VERSION_NUMBER + "-r" + REVISION_NUMBER;
    }

    public static void main(String[] args) {
        // START - ALLOW PRINT STATEMENTS
        System.out.println(new OpenJPAVersion().toString());
        // STOP - ALLOW PRINT STATEMENTS
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(80 * 40);
        appendOpenJPABanner(buf);
        buf.append("\n");

        appendProperty("os.name", buf).append("\n");
        appendProperty("os.version", buf).append("\n");
        appendProperty("os.arch", buf).append("\n\n");

        appendProperty("java.version", buf).append("\n");
        appendProperty("java.vendor", buf).append("\n\n");

        buf.append("java.class.path:\n");
        StringTokenizer tok =
            new StringTokenizer(AccessController.doPrivileged(J2DoPrivHelper
                .getPropertyAction("java.class.path")), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            buf.append("\t").append(tok.nextToken());
            buf.append("\n");
        }
        buf.append("\n");

        appendProperty("user.dir", buf);
        return buf.toString();
    }

    public void appendOpenJPABanner(StringBuilder buf) {
        buf.append(VENDOR_NAME).append(" ");
        buf.append(VERSION_NUMBER);
        buf.append("\n");
        buf.append(_loc.get("version-id")).append(": ").append(VERSION_ID);
        buf.append("\n");
        buf.append(_loc.get("openjpa-revision")).append(": ").append(REVISION_NUMBER);
        buf.append("\n");
    }

    private StringBuilder appendProperty(String prop, StringBuilder buf) {
        return buf.append(prop).append(": ").append(
            AccessController.doPrivileged(J2DoPrivHelper
                .getPropertyAction(prop)));
    }
}
