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
package org.apache.openejb.arquillian.tests;

import org.apache.ziplock.JarLocation;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public abstract class TestSetup {

    public WebArchive createDeployment(Class...archiveClasses) {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0");
        decorateDescriptor(descriptor);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, getTestContextName() + ".war")
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
        
        if (archiveClasses != null) {
            for (Class c: archiveClasses) {
                archive.addClass(c);
            }
        }
        decorateArchive(archive);

        return archive;
    }

    protected String getTestContextName() {
        return this.getClass().getSimpleName();
    }

    protected void decorateDescriptor(WebAppDescriptor descriptor) {

    }

    protected void decorateArchive(WebArchive archive) {

    }

    protected void validateTest(String expectedOutput) throws IOException {
        validateTest(getTestContextName(), expectedOutput);
    }

    protected void validateTest(String servlet, String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:" + System.getProperty("tomee.http.port", "11080") + "/" + getTestContextName() + "/" + servlet).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead = -1;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput + "\n" + output, output.contains(expectedOutput));
    }

}
