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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OpenEJB Enterprise Javabean Protocol (OEJP)
 *
 * OEJP uses a "<major>.<minor>" numbering scheme to indicate versions of the protocol.
 *
 *     Protocol-Version   = "OEJP" "/" 1*DIGIT "." 1*DIGIT
 *
 * Some compatability is guaranteed with the major part of the version number.
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 15:13:22 -0500 (Fri, 29 Sep 2006) $
 */
public class ProtocolMetaData {

    private static final String OEJB = "OEJP";
    private String id;
    private int major;
    private int minor;

    public ProtocolMetaData() {
    }

    public ProtocolMetaData(String version) {
        init(OEJB+"/"+version);
    }

    private void init(String spec) {
        assert spec.matches("^OEJP/[0-9]\\.[0-9]$"): "Protocol version spec must follow format [ \"OEJB\" \"/\" 1*DIGIT \".\" 1*DIGIT ]";

        char[] chars = new char[8];
        spec.getChars(0, chars.length, chars, 0);

        this.id = new String(chars, 0, 4);
        this.major = Integer.parseInt(new String(chars, 5,1));
        this.minor = Integer.parseInt(new String(chars, 7,1));
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
        return major+"."+minor;
    }

    public String getSpec() {
        return id+"/"+major+"."+minor;
    }

    public void writeExternal(OutputStream out) throws IOException {
        out.write(getSpec().getBytes("UTF-8"));
    }

    public void readExternal(InputStream in) throws IOException {
        byte[] spec = new byte[8];
        for (int i = 0; i < spec.length; i++) {
            spec[i] = (byte) in.read();
            if (spec[i] == -1){
                throw new IOException("Unable to read protocol version.  Reached the end of the stream.");
            }
        }
        init(new String(spec,"UTF-8"));
    }
}
