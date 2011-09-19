/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.tomcat.loader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;

import org.apache.openejb.tomcat.loader.filter.Filter;
import org.apache.openejb.tomcat.loader.filter.Filters;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.StandardJarScanner;

public class TomEEJarScanner extends StandardJarScanner {

	public void scan(ServletContext context, ClassLoader classLoader, JarScannerCallback callback, Set<String> jarsToIgnore) {
		String openejbWar = System.getProperty("openejb.war");

        if (openejbWar == null) {
            EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
            return;
        }

		Set<String> newIgnores = new HashSet<String>();
		if (jarsToIgnore != null) {
			newIgnores.addAll(jarsToIgnore);
		}

		if (openejbWar != null && "FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
			File openejbApp = new File(openejbWar);
			File libFolder = new File(openejbApp, "lib");
			for (File f : libFolder.listFiles()) {
				if (f.getName().toLowerCase().endsWith(".jar")) {
					newIgnores.add(f.getName());
				}
			}
		}
		
		super.scan(context, classLoader, callback, newIgnores);
	}

    public static UrlSet applyBuiltinExcludes(UrlSet urlSet) throws MalformedURLException {

        Filter filter = Filters.prefixes(
                "XmlSchema-",
                "activeio-",
                "activemq-",
                "antlr-",
                "aopalliance-",
                "avalon-framework-",
                "axis-",
                "axis2-",
                "bcprov-",
                "bsh-",
                "bval-core",
                "bval-jsr",
                "catalina-",
                "cglib-",
                "commons-beanutils",
                "commons-cli-",
                "commons-codec-",
                "commons-collections-",
                "commons-dbcp",
                "commons-dbcp-all-1.3-",
                "commons-discovery-",
                "commons-httpclient-",
                "commons-io-",
                "commons-lang-",
                "commons-logging-",
                "commons-logging-api-",
                "commons-net-",
                "commons-pool-",
                "cssparser-",
                "cxf-",
                "deploy.jar",
                "derby-",
                "dom4j-",
                "geronimo-",
                "gragent.jar",
                "guice-",
                "hibernate-",
                "howl-",
                "hsqldb-",
                "htmlunit-",
                "icu4j-",
                "idb-",
                "idea_rt.jar",
                "jasypt-",
                "javaee-",
                "javaee-api",
                "javassist-",
                "javaws.jar",
                "javax.",
                "jaxb-",
                "jaxp-",
                "jboss-",
                "jbossall-",
                "jbosscx-",
                "jbossjts-",
                "jbosssx-",
                "jcommander-",
                "jetty-",
                "jmdns-",
                "jsp-api-",
                "jsr299-",
                "jsr311-",
                "juli-",
                "junit-",
                "kahadb-",
                "log4j-",
                "logkit-",
                "myfaces-",
                "neethi-",
                "nekohtml-",
                "openejb-api",
                "openejb-javaagent",
                "openejb-jee",
                "openejb-loader",
                "openjpa-",
                "opensaml-",
                "openwebbeans-",
                "org.eclipse.",
                "org.junit.",
                "org.osgi.core-",
                "quartz-",
                "rmock-",
                "saaj-",
                "sac-",
                "scannotation-",
                "serializer-",
                "serp-",
                "servlet-api-",
                "slf4j-",
                "spring-",
                "stax-api-",
                "swizzle-",
                "testng-",
                "webbeans-ee",
                "webbeans-ejb",
                "webbeans-impl",
                "webbeans-spi",
                "wsdl4j-",
                "wss4j-",
                "wstx-asl-",
                "xalan-",
                "xbean-",
                "xercesImpl-",
                "xml-apis-",
                "xml-resolver-",
                "xmlrpc-",
                "xmlsec-",
                "xmlunit-",
                "aether-api-",
                "aether-connector-wagon-",
                "aether-impl-",
                "aether-spi-",
                "aether-util-",
                "arquillian-api-",
                "arquillian-impl-base-",
                "arquillian-junit-",
                "arquillian-protocol-servlet-",
                "arquillian-spi-",
                "arquillian-testenricher-cdi-",
                "arquillian-testenricher-ejb-",
                "arquillian-testenricher-resource-",
                "cdi-api-",
                "commons-digester-",
                "ecj-",
                "google-collections-",
                "jettison-",
                "joda-time-",
                "jsr250-api-",
                "jstl-",
                "maven-aether-provider-",
                "maven-model-",
                "maven-model-builder-",
                "maven-repository-metadata-",
                "maven-settings-",
                "maven-settings-builder-",
                "openws-",
                "oro-",
                "plexus-cipher-",
                "plexus-classworlds-",
                "plexus-component-annotations-",
                "plexus-container-default-",
                "plexus-interpolation-",
                "plexus-sec-dispatcher-",
                "plexus-utils-",
                "shrinkwrap-api-",
                "shrinkwrap-descriptors-api-",
                "shrinkwrap-impl-base-",
                "shrinkwrap-resolver-api-",
                "shrinkwrap-resolver-api-maven-",
                "shrinkwrap-resolver-impl-maven-",
                "shrinkwrap-spi-",
                "tomcat-annotations-api-",
                "tomcat-api-",
                "tomcat-catalina-",
                "tomcat-coyote-",
                "tomcat-el-api-",
                "tomcat-jasper-",
                "tomcat-jasper-el-",
                "tomcat-jsp-api-",
                "tomcat-juli-",
                "tomcat-servlet-api-",
                "tomcat-util-",
                "velocity-",
                "wagon-file-",
                "wagon-http-lightweight-",
                "wagon-http-shared-",
                "wagon-provider-api-",
                "woodstox-core-asl-",
                "xmlschema-core-",
                "xmltooling-"
        );

//        filter = Filters.optimize(filter, new PatternFilter(".*/openejb-.*"));
        List<URL> urls = urlSet.getUrls();
        Iterator<URL> iterator = urls.iterator();
        while (iterator.hasNext()) {
            URL url = iterator.next();
            File file = toFile(url);

            String name = filter(file).getName();
//            System.out.println("JAR "+name);
            if (filter.accept(name)) iterator.remove();
        }



        return new UrlSet(urls);
    }

    private static File filter(File location) {
        List<String> invalid = new ArrayList<String>();
        invalid.add("classes");
        invalid.add("test-classes");
        invalid.add("target");
        invalid.add("build");
        invalid.add("dist");
        invalid.add("bin");

        while (invalid.contains(location.getName())) {
            location = location.getParentFile();
        }
        return location;
    }

        public static File toFile(URL url) {
        if ("jar".equals(url.getProtocol())) {
            try {
                String spec = url.getFile();

                int separator = spec.indexOf('!');
                /*
                 * REMIND: we don't handle nested JAR URLs
                 */
                if (separator == -1) throw new MalformedURLException("no ! found in jar url spec:" + spec);

                return toFile(new URL(spec.substring(0, separator++)));
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else if ("file".equals(url.getProtocol())) {
            return new File(importme(url));
        } else {
            throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
        }
    }

    private static String importme(URL url) {
        String fileName = url.getFile();
        if (fileName.indexOf('%') == -1) return fileName;

        StringBuilder result = new StringBuilder(fileName.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < fileName.length();) {
            char c = fileName.charAt(i);

            if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= fileName.length()) {
                        throw new IllegalArgumentException("Incomplete % sequence at: " + i);
                    }

                    int d1 = Character.digit(fileName.charAt(i + 1), 16);
                    int d2 = Character.digit(fileName.charAt(i + 2), 16);

                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + String.valueOf(i));
                    }

                    out.write((byte) ((d1 << 4) + d2));

                    i += 3;

                } while (i < fileName.length() && fileName.charAt(i) == '%');


                result.append(out.toString());

                continue;
            } else {
                result.append(c);
            }

            i++;
        }
        return result.toString();
    }

    public static String toFilePath(URL url) {
        return toFile(url).getAbsolutePath();
    }
}
