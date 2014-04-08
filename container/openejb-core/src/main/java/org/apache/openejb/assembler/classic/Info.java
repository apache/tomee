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

import org.apache.openejb.OpenEJBRuntimeException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class Info {
    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Info.class);
        } catch (JAXBException e) {
            throw new OpenEJBRuntimeException("can't create jaxbcontext for Info class");
        }
    }

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

    public static void marshal(AppInfo appInfo, Writer out) throws JAXBException {
        marshaller().marshal(new Info(appInfo), out);
    }

    public static AppInfo unmarshal(InputStream in) throws JAXBException {
        return ((Info) unmarshaller().unmarshal(in)).appInfo;
    }

    private static Marshaller marshaller() throws JAXBException {
        final Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        return marshaller;
    }

    private static Unmarshaller unmarshaller() throws JAXBException {
        return JAXB_CONTEXT.createUnmarshaller();
    }
}
