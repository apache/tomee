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
package org.apache.openejb.alt.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DTDResolver implements EntityResolver {
    public static HashMap dtds = new HashMap();

    static {
        byte[] bytes = getDtd("ejb-jar_1_1.dtd");
        if (bytes != null) {
            dtds.put("ejb-jar.dtd", bytes);
            dtds.put("ejb-jar_1_1.dtd", bytes);
        }
        bytes = getDtd("ejb-jar_2_0.dtd");
        if (bytes != null) {
            dtds.put("ejb-jar_2_0.dtd", bytes);
        }
    }

    public static byte[] getDtd(String dtdName) {
        try {

            URL dtd = new URL("resource:/schema/" + dtdName);
            InputStream in = dtd.openStream();
            if (in == null) return null;

            byte[] buf = new byte[512];

            in = new BufferedInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int count;
            while ((count = in.read(buf)) > -1) out.write(buf, 0, count);

            in.close();
            out.close();

            return out.toByteArray();
        } catch (Throwable e) {
            return null;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        int pos = systemId.lastIndexOf('/');
        if (pos != -1) {
            systemId = systemId.substring(pos + 1);
        }

        byte[] data = (byte[]) dtds.get(systemId);

        if (data != null) {
            return new InputSource(new ByteArrayInputStream(data));
        } else {
            return null;
        }
    }
}
