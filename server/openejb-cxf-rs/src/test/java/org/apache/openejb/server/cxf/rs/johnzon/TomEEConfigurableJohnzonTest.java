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

import org.apache.johnzon.mapper.Converter;
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
import java.util.Comparator;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class TomEEConfigurableJohnzonTest {
    @RandomPort("http")
    private URL base;

    @Module
    public static EjbModule service() throws Exception {
        final EjbModule module = new EjbModule(new EjbJar(), new OpenejbJar());
        final EnterpriseBean bean = new SingletonBean(Endpoint.class).localBean();
        module.getEjbJar().addEnterpriseBean(bean);

        final Resources resources = new Resources();

        final Service sorter = new Service("testSorter", null);
        sorter.setClassName(Sorter.class.getName());
        resources.getService().add(sorter);


        final Service converter = new Service("customerConverter", null);
        converter.setClassName(MyConverter.class.getName());
        resources.getService().add(converter);

        final Service johnzon = new Service("johnzon", null);
        johnzon.setClassName(TomEEConfigurableJohnzon.class.getName());
        johnzon.getProperties().put("datePattern", "yyyy");
        // johnzon.getProperties().put("converter", "$customerConverter"); // or the collection syntax
        johnzon.getProperties().put("converters", "collection:$customerConverter,$customerConverter");
        johnzon.getProperties().put("attributeOrder", "$testSorter");
        resources.getService().add(johnzon);

        module.initResources(resources);

        final PojoDeployment e = new PojoDeployment();
        e.setClassName("jaxrs-application");
        e.getProperties().setProperty("cxf.jaxrs.providers", "johnzon");
        module.getOpenejbJar().getPojoDeployment().add(e);

        return module;
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"date\":\"" + year() + "\",\"horrible\":\"awesome\"}", IO.slurp(new URL(base.toExternalForm() + getClass().getSimpleName() + "/test")));
    }

    private String year() { // same johnzon should have done
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Model get() {
            final Model model = new Model();
            model.setDate(new Date());
            model.setHorrible(new Horrible());
            return model;
        }
    }

    public static class Model {
        private Horrible horrible;
        private Date date;

        public Horrible getHorrible() {
            return horrible;
        }

        public void setHorrible(final Horrible horrible) {
            this.horrible = horrible;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }
    }

    public static class Horrible {
    }


    public static class MyConverter implements Converter<Horrible> {
        @Override
        public String toString(final Horrible instance) {
            return "awesome";
        }

        @Override
        public Horrible fromString(final String text) {
            return new Horrible();
        }
    }

    public static class Sorter implements Comparator<String> {
        @Override
        public int compare(final String o1, final String o2) {
            return o1.compareTo(o2);
        }
    }
}
