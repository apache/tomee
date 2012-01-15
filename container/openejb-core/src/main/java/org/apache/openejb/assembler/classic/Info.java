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
package org.apache.openejb.assembler.classic;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.OutputStream;

/**
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class Info {

    public AppInfo appInfo;

    public Info(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public Info() {
    }

    public static void marshal(AppInfo appInfo) throws JAXBException {
        marshal(appInfo, System.out);
    }

    public static void marshal(AppInfo appInfo, OutputStream out) throws JAXBException {
        marshaller().marshal(new Info(appInfo), out);
    }

    private static Marshaller marshaller() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Info.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        return marshaller;
    }

}
