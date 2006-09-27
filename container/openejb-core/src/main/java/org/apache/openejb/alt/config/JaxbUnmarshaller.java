/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.JarUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JaxbUnmarshaller {

    private final Class clazz;
    private final File xmlFile;

    private javax.xml.bind.Unmarshaller unmarshaller;

    public JaxbUnmarshaller(Class type, String xmlFileName) throws OpenEJBException {
        this.clazz = type;
        this.xmlFile = new File(xmlFileName);
        try {
            JAXBContext ctx = JAXBContext.newInstance(type);
            unmarshaller = ctx.createUnmarshaller();
        } catch (JAXBException e) {
            throw new OpenEJBException("Could not create a JAXBContext for class " + type.getName(), e);
        }
    }

    public static Object unmarshal(Class clazz, String xmlFile, String jarLocation) throws OpenEJBException {
        return new JaxbUnmarshaller(clazz, xmlFile).unmarshal(jarLocation);
    }


    public static Object unmarshal(Class clazz, String xmlFile) throws OpenEJBException {
        try {
            if (xmlFile.startsWith("jar:")) {
                URL url = new URL(xmlFile);
                xmlFile = url.getFile();
            }
            if (xmlFile.startsWith("file:")) {
                URL url = new URL(xmlFile);
                xmlFile = url.getFile();
            }
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Unable to resolve location " + xmlFile, e);
        }

        String jarLocation = null;
        int jarSeparator = xmlFile.indexOf("!");
        if (jarSeparator > 0) {
            jarLocation = xmlFile.substring(0, jarSeparator);
            xmlFile = xmlFile.substring(jarSeparator + 2);
        } else {
            File file = new File(xmlFile);
            xmlFile = file.getName();
            jarLocation = file.getParent();
        }

        return new JaxbUnmarshaller(clazz, xmlFile).unmarshal(jarLocation);
    }

    public Object unmarshal(String location) throws OpenEJBException {
        File file = new File(location);
        if (file.isDirectory()) {
            return unmarshalFromDirectory(file);
        } else {
            return unmarshalFromJar(file);
        }
    }

    public Object unmarshalFromJar(File jarFile) throws OpenEJBException {
        String jarLocation = jarFile.getPath();
        String file = xmlFile.getName();

        JarFile jar = JarUtils.getJarFile(jarLocation);
        JarEntry entry = jar.getJarEntry(xmlFile.getPath().replaceAll("\\\\", "/"));

        if (entry == null)
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotFindFile", file, jarLocation));

        Reader reader = null;
        InputStream stream = null;

        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader(stream);
            return unmarshalObject(reader, file, jarLocation);
        } catch (IOException e) {
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotRead", file, jarLocation, e.getLocalizedMessage()));
        } finally {
            try {
                if (stream != null) stream.close();
                if (reader != null) reader.close();
                if (jar != null) jar.close();
            } catch (Exception e) {
                throw new OpenEJBException(EjbJarUtils.messages.format("file.0020", jarLocation, e.getLocalizedMessage()));
            }
        }
    }

    public Object unmarshalFromDirectory(File directory) throws OpenEJBException {
        String file = xmlFile.getName();

        Reader reader = null;
        InputStream stream = null;

        try {
            File fullPath = new File(directory, xmlFile.getPath());
            stream = new FileInputStream(fullPath);
            reader = new InputStreamReader(stream);
            return unmarshalObject(reader, file, directory.getPath());
        } catch (FileNotFoundException e) {
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotFindFile", xmlFile.getPath(), directory.getPath()), e);
        } finally {
            try {
                if (stream != null) stream.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                throw new OpenEJBException(EjbJarUtils.messages.format("file.0020", directory.getPath(), e.getLocalizedMessage()));
            }
        }
    }

    public Object unmarshal(URL url) throws OpenEJBException {
        String file = xmlFile.getName();

        Reader reader = null;
        InputStream stream = null;

        try {
            URL fullURL = new URL(url, xmlFile.getPath());
            stream = fullURL.openConnection().getInputStream();
            reader = new InputStreamReader(stream);
            return unmarshalObject(reader, file, fullURL.getPath());
        } catch (MalformedURLException e) {
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotFindFile", file, url.getPath()));
        } catch (IOException e) {
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotRead", file, url.getPath(), e.getLocalizedMessage()));
        } finally {
            try {
                if (stream != null) stream.close();
                if (reader != null) reader.close();
            } catch (Exception e) {
                throw new OpenEJBException(EjbJarUtils.messages.format("file.0020", url.getPath(), e.getLocalizedMessage()));
            }
        }
    }

    private Object unmarshalObject(Reader reader, String file, String jarLocation) throws OpenEJBException {
        try {
            Object object = unmarshaller.unmarshal(reader);
            if (object instanceof JAXBElement) {
                JAXBElement element = (JAXBElement) object;
                object = element.getValue();
            }
            return object;
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new OpenEJBException(EjbJarUtils.messages.format("xml.cannotUnmarshal", file, jarLocation, e.getLocalizedMessage()));
        }
    }
}
