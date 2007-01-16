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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.sys.ServiceProvider;
import org.apache.openejb.alt.config.sys.ServicesJar;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServiceUtils {

    public static final String defaultProviderURL = "org.apache.openejb";
    private static Map loadedServiceJars = new HashMap();
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    public static ServiceProvider getServiceProvider(String id) throws OpenEJBException {

        String providerName = null;
        String serviceName = null;

        if (id.indexOf("#") == -1) {
            providerName = defaultProviderURL;
            serviceName = id;
        } else {
            providerName = id.substring(0, id.indexOf("#"));
            serviceName = id.substring(id.indexOf("#") + 1);
        }

        ServiceProvider service = null;

        if (loadedServiceJars.get(providerName) == null) {
            ServicesJar sj = readServicesJar(providerName);
            ServiceProvider[] sp = sj.getServiceProvider();
            HashMap services = new HashMap(sj.getServiceProviderCount());

            for (int i = 0; i < sp.length; i++) {
                services.put(sp[i].getId(), sp[i]);
            }

            loadedServiceJars.put(providerName, services);

            service = (ServiceProvider) services.get(serviceName);
        } else {
            Map provider = (Map) loadedServiceJars.get(providerName);
            service = (ServiceProvider) provider.get(serviceName);
        }

        if (service == null) {
            throw new OpenEJBException(messages.format("conf.4901", serviceName, providerName));
        }

        return service;
    }

    public static ServicesJar readServicesJar(String providerName) throws OpenEJBException {
        try {
            Unmarshaller unmarshaller = new Unmarshaller(ServicesJar.class, "service-jar.xml");
            URL serviceURL = new URL("resource:/META-INF/" + providerName + "/");
            return (ServicesJar) unmarshaller.unmarshal(serviceURL);
        } catch (MalformedURLException e) {
            throw new OpenEJBException(e);
        }
    }

    public static void writeServicesJar(String xmlFile, ServicesJar servicesJarObject) throws OpenEJBException {

        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;

        try {
            File file = new File(xmlFile);
            writer = new FileWriter(file);
            servicesJarObject.marshal(writer);
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("conf.4040", xmlFile, e.getLocalizedMessage()));
        } catch (MarshalException e) {
            if (e.getCause() instanceof IOException) {
                throw new OpenEJBException(messages.format("conf.4040", xmlFile, e.getLocalizedMessage()));
            } else {
                throw new OpenEJBException(messages.format("conf.4050", xmlFile, e.getLocalizedMessage()));
            }
        } catch (ValidationException e) {

            /* TODO: Implement informative error handling here.
               The exception will say "X doesn't match the regular
               expression Y"
               This should be checked and more relevant information
               should be given -- not everyone understands regular
               expressions.
             */

            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */

            throw new OpenEJBException(messages.format("conf.4060", xmlFile, e.getLocalizedMessage()));
        }

        try {
            writer.close();
        } catch (Exception e) {
            throw new OpenEJBException(messages.format("file.0020", xmlFile, e.getLocalizedMessage()));
        }
    }

    public static Properties loadProperties(String pFile) throws OpenEJBException {
        return loadProperties(pFile, new Properties());
    }

    public static Properties loadProperties(String propertiesFile, Properties defaults) throws OpenEJBException {
        try {
            File pfile = new File(propertiesFile);
            InputStream in = new FileInputStream(pfile);

            try {
                /*
                This may not work as expected.  The desired effect is that
                the load method will read in the properties and overwrite
                the values of any properties that may have previously been
                defined.
                */
                defaults.load(in);
            } catch (IOException ex) {
                throw new OpenEJBException(messages.format("conf.0012", ex.getLocalizedMessage()));
            }

            return defaults;
        } catch (FileNotFoundException ex) {
            throw new OpenEJBException(messages.format("conf.0006", propertiesFile, ex.getLocalizedMessage()));
        } catch (IOException ex) {
            throw new OpenEJBException(messages.format("conf.0007", propertiesFile, ex.getLocalizedMessage()));
        } catch (SecurityException ex) {
            throw new OpenEJBException(messages.format("conf.0005", propertiesFile, ex.getLocalizedMessage()));
        }
    }

}
