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
package org.apache.openejb.config;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.MethodParams;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.QueryMethod;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Connector;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.OpenEjbVersion;

/**
 * Deploy EJB beans
 */
public class Deploy {
    public static void main(String[] args) {
        // TODO: Use the deploy bean here instead
        // This class could simply be a nice command line version
        // of the client for that tool
        System.out.println("Place application in the apps/ directory are restart the server");
        System.out.println("");
        System.out.println("Hot deploy is supported in the codebase, but not yet hooked up.");
        System.out.println("This tool will be a command-line version of hot deploy allowing");
        System.out.println("you to specify options and see success/failure on the deploy.");
    }
}