/**
 *
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
package org.apache.tomee.catalina;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.VirtualWebappLoader;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.CompositeClassLoaderConfigurer;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.reflection.Reflections;

import java.io.File;

/**
 * Usage example in META-INF/context.xml
 *

 <Context antiJARLocking="true" >
     <Loader
         className="org.apache.tomee.catalina.ProvisioningWebappLoader"
         searchExternalFirst="true"
         virtualClasspath="mvn:commons-el:commons-el:1.0;mvn:commons-el:commons-el:1.0"
         searchVirtualFirst="true"
     />
 </Context>

 *
 */
public class ProvisioningWebappLoader extends VirtualWebappLoader {
	private static final boolean SKIP_BACKGROUND_PROCESS = "true".equals(SystemInstance.get().getProperty("tomee.classloader.backgroundProcess", "true"));

    @Override
    public void backgroundProcess() {
		if (SKIP_BACKGROUND_PROCESS) {
			return;
		}

        final ClassLoader classloader = super.getClassLoader();
        if (classloader instanceof LazyStopWebappClassLoader) {
            final LazyStopWebappClassLoader lazyStopWebappClassLoader = (LazyStopWebappClassLoader) classloader;
            lazyStopWebappClassLoader.restarting();
            try {
                super.backgroundProcess();
            } finally {
                lazyStopWebappClassLoader.restarted();
            }
        } else {
            super.backgroundProcess();
        }
    }

	@Override
	public boolean modified() {
		return !SKIP_BACKGROUND_PROCESS;
	}

	@Override
    protected void startInternal() throws LifecycleException {
        // standard tomcat part
        final StringBuilder builder = new StringBuilder();
        final String classpath = String.class.cast(Reflections.get(this, "virtualClasspath"));
        if (classpath != null && !classpath.isEmpty()) {
            for (final String s : String.class.cast(classpath).split(";")) {
                builder.append(ProvisioningUtil.realLocation(s)).append(";");
            }
        }

        ClassLoaderConfigurer configurer = ClassLoaderUtil.configurer(getContainer().getName());

        // WEB-INF/jars.xml
        if (Context.class.isInstance(getContainer())) {
            final File war = Contexts.warPath(Context.class.cast(getContainer()));
            final File jarsXml = new File(war, "WEB-INF/" + QuickJarsTxtParser.FILE_NAME);
            final ClassLoaderConfigurer configurerTxt = QuickJarsTxtParser.parse(jarsXml);
            if (configurerTxt != null) {
                configurer = new CompositeClassLoaderConfigurer(configurer, configurerTxt);
            }
        }

        // clean up builder and set classpath to delegate to parent init
        String cp = builder.toString();
        if (cp.endsWith(";")) {
            cp = cp.substring(0, cp.length() - 1);
        }
        Reflections.set(this, "virtualClasspath", cp);

        LazyStopWebappClassLoader.initContext(configurer);
        try {
            super.startInternal();
        } finally {
            LazyStopWebappClassLoader.cleanInitContext();
        }
    }

    @Override
    public String toString() {
        return "Provisioning" + super.toString();
    }
}
