/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs.johnzon;

import org.apache.johnzon.mapper.access.FieldAccessMode;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class TomEEJsonbProviderTest {
    @RandomPort("http")
    private URL base;

    @Module
    public static EjbModule service(){
        final EjbModule module = new EjbModule(new EjbJar(), new OpenejbJar());
        final EnterpriseBean bean = new SingletonBean(Endpoint.class).localBean();
        module.getEjbJar().addEnterpriseBean(bean);

        final Resources resources = new Resources();

        final Service configuredTomEEJsonbProvider = new Service("configuredTomEEJsonbProvider", null);
        configuredTomEEJsonbProvider.setClassName(TomEEJsonbProvider.class.getName());
        configuredTomEEJsonbProvider.getProperties().put("dateFormat", "MMM-yyyy");
        configuredTomEEJsonbProvider.getProperties().put("locale", Locale.GERMANY);
        configuredTomEEJsonbProvider.getProperties().put("accessMode", new FieldAccessMode(true,true));

        resources.getService().add(configuredTomEEJsonbProvider);

        module.initResources(resources);

        final PojoDeployment e = new PojoDeployment();
        e.setClassName("jaxrs-application");
        e.getProperties().setProperty("cxf.jaxrs.providers", "configuredTomEEJsonbProvider");
        module.getOpenejbJar().getPojoDeployment().add(e);

        return module;
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"date\":\"" + localizedFormattedDate() + "\",\"fieldAccessOnly\":\"access-via-field\"}", IO.slurp(new URL(base.toExternalForm() + getClass().getSimpleName() + "/test")));
    }

    private String localizedFormattedDate() { // same configuredTomEEJsonbProvider should have done
        return new SimpleDateFormat("MMM-yyyy", Locale.GERMANY).format(new Date());
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Model get() {
            final Model model = new Model();
            model.setDate(new Date());
            return model;
        }
    }

    public static class Model {
        private Date date;
        private String fieldAccessOnly = "access-via-field";

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public String getSomeField() {
            throw new UnsupportedOperationException("This Getter should not be invoked as we configured field-access.");
        }
    }
}
