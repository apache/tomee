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

package org.apache.openejb.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * OpenEJB Enterprise Javabean Protocol (OEJP)
 * <p/>
 * OEJP uses a "<major>.<minor>" numbering scheme to indicate versions of the protocol.
 * <p/>
 * Protocol-Version   = "OEJP" "/" 1*DIGIT "." 1*DIGIT
 * <p/>
 * Some compatability is guaranteed with the major part of the version number.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("UnusedDeclaration")
public class ProtocolMetaData {

    public static final String VERSION = "4.6";

    private static final String OEJB = "OEJP";
    private transient String id;
    private transient int major;
    private transient int minor;

    public ProtocolMetaData() {
        init(OEJB + "/" + VERSION);
    }

    public ProtocolMetaData(final String version) {
        init(OEJB + "/" + version);
    }

    private void init(final String spec) {

        if (!spec.matches("^OEJP/[0-9]\\.[0-9]$")) {
            throw new RuntimeException("Protocol version spec must follow format [ \"OEJB\" \"/\" 1*DIGIT \".\" 1*DIGIT ] - " + spec);
        }

        final char[] chars = new char[8];
        spec.getChars(0, chars.length, chars, 0);

        this.id = new String(chars, 0, 4);
        this.major = Integer.parseInt(new String(chars, 5, 1));
        this.minor = Integer.parseInt(new String(chars, 7, 1));
    }

    public boolean isAtLeast(final int major, final int minor) {
        return this.major >= major && (this.major != major || this.minor >= minor);
    }

    public String getId() {
        return id;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getVersion() {
        return major + "." + minor;
    }

    public String getSpec() {
        return id + "/" + major + "." + minor;
    }

    public void writeExternal(final OutputStream out) throws IOException {
        out.write(getSpec().getBytes("UTF-8"));
        out.flush();
    }

    public void readExternal(final InputStream in) throws IOException {
        final byte[] spec = new byte[8];
        for (int i = 0; i < spec.length; i++) {
            spec[i] = (byte) in.read();
            if (spec[i] == -1) {
                throw new EOFException("Unable to read protocol version.  Reached the end of the stream.");
            }
        }
        try {
            init(new String(spec, "UTF-8"));
        } catch (Throwable e) {
            throw new IOException("Failed to read spec: " + Arrays.toString(spec), e);
        }
    }
}
