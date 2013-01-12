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
package org.apache.openejb.sxc;

import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesConfig$JAXB;
import org.apache.openejb.loader.IO;

import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class FacesConfigXml {

    public static FacesConfig unmarshal(InputStream inputStream) throws Exception {
        return Sxc.unmarshalJavaee(new FacesConfig$JAXB(), inputStream);
    }

    public static FacesConfig unmarshal(URL url) throws Exception {
        final InputStream inputStream = IO.read(url);
        try {
            return Sxc.unmarshalJavaee(new FacesConfig$JAXB(), inputStream);
        } finally {
            IO.close(inputStream);
        }
    }

    public static void marshal(FacesConfig facesConfig, OutputStream outputStream) throws Exception {
        Sxc.marshal(new FacesConfig$JAXB(), facesConfig, new StreamResult(outputStream));
    }
}
