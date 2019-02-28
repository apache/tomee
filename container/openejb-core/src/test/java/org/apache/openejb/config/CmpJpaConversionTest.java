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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.persistence.Id;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class CmpJpaConversionTest {


    @Test
    public void shouldDeployAppModule() throws OpenEJBException {


        AppModule appModule = new AppModule(this.getClass().getClassLoader(), "app");

        EntityMappings entityMappings = new EntityMappings();
        Entity entity = new Entity();
        EntityBean entityBean = new EntityBean(PersonBean.class.getName(), Person.class.getName(), PersistenceType.CONTAINER);

        EjbJar ejbJar = new EjbJar();
        EjbModule ejbModule = new EjbModule(ejbJar);
        OpenejbJar openejbJar = new OpenejbJar();

        appModule.getEjbModules().add(ejbModule);
        ejbJar.addEnterpriseBean(entityBean);
        ejbModule.setOpenejbJar(openejbJar);
        CmpJpaConversion conversion = new CmpJpaConversion();

        AppModule deploy = conversion.deploy(appModule);
        Assert.assertNotNull(deploy);

        List<PersistenceModule> persistenceModules = appModule.getPersistenceModules();
        Assert.assertEquals(1, persistenceModules.size());
        PersistenceModule persistenceModule = persistenceModules.get(0);
        Assert.assertEquals(appModule.getModuleUri().toString(), persistenceModule.getRootUrl());
    }

    @Test
    public void shouldDeployAppModuleWithoutEJBModule() throws OpenEJBException {


        AppModule appModule = new AppModule(this.getClass().getClassLoader(), "app");

        EntityMappings entityMappings = new EntityMappings();
        Entity entity = new Entity();
        EntityBean entityBean = new EntityBean(PersonBean.class.getName(), Person.class.getName(), PersistenceType.CONTAINER);

        CmpJpaConversion conversion = new CmpJpaConversion();

        AppModule deploy = conversion.deploy(appModule);
        Assert.assertNotNull(deploy);

        List<PersistenceModule> persistenceModules = appModule.getPersistenceModules();
        Assert.assertTrue(persistenceModules.isEmpty());
    }


    @Test
    public void shouldDeployAppModuleUsingModuleAsContructor() throws OpenEJBException {

        EntityMappings entityMappings = new EntityMappings();
        Entity entity = new Entity();
        EntityBean entityBean = new EntityBean(PersonBean.class.getName(), Person.class.getName(), PersistenceType.CONTAINER);

        EjbJar ejbJar = new EjbJar();
        EjbModule ejbModule = new EjbModule(ejbJar);
        OpenejbJar openejbJar = new OpenejbJar();

        ejbJar.addEnterpriseBean(entityBean);
        ejbModule.setOpenejbJar(openejbJar);

        AppModule appModule = new AppModule(ejbModule);
        CmpJpaConversion conversion = new CmpJpaConversion();

        AppModule deploy = conversion.deploy(appModule);
        Assert.assertNotNull(deploy);

        List<PersistenceModule> persistenceModules = appModule.getPersistenceModules();
        Assert.assertEquals(1, persistenceModules.size());
        PersistenceModule persistenceModule = persistenceModules.get(0);
        Assert.assertEquals(appModule.getModuleUri().toString(), persistenceModule.getRootUrl());
    }


    @Test
    public void shouldDeployAppModuleWhenSetModuleId() throws OpenEJBException {

        AppModule appModule = new AppModule(this.getClass().getClassLoader(), "app");

        EntityMappings entityMappings = new EntityMappings();
        Entity entity = new Entity();
        EntityBean entityBean = new EntityBean(PersonBean.class.getName(), Person.class.getName(), PersistenceType.CONTAINER);

        EjbJar ejbJar = new EjbJar();
        EjbModule ejbModule = new EjbModule(ejbJar);
        OpenejbJar openejbJar = new OpenejbJar();

        appModule.getEjbModules().add(ejbModule);
        appModule.setModuleId("moduleId");
        ejbJar.addEnterpriseBean(entityBean);
        ejbModule.setOpenejbJar(openejbJar);
        CmpJpaConversion conversion = new CmpJpaConversion();

        AppModule deploy = conversion.deploy(appModule);
        Assert.assertNotNull(deploy);

        List<PersistenceModule> persistenceModules = appModule.getPersistenceModules();
        Assert.assertEquals(1, persistenceModules.size());
        PersistenceModule persistenceModule = persistenceModules.get(0);
        Assert.assertEquals(appModule.getModuleUri().toString(), persistenceModule.getRootUrl());
    }


    @Test
    public void shouldDeployAppModuleWhenSetModuleIdAsNull() throws OpenEJBException {

        AppModule appModule = new AppModule(this.getClass().getClassLoader(), "app");

        EntityMappings entityMappings = new EntityMappings();
        Entity entity = new Entity();
        EntityBean entityBean = new EntityBean(PersonBean.class.getName(), Person.class.getName(), PersistenceType.CONTAINER);

        EjbJar ejbJar = new EjbJar();
        EjbModule ejbModule = new EjbModule(ejbJar);
        OpenejbJar openejbJar = new OpenejbJar();

        appModule.getEjbModules().add(ejbModule);
        appModule.setModuleId(null);
        ejbJar.addEnterpriseBean(entityBean);
        ejbModule.setOpenejbJar(openejbJar);
        CmpJpaConversion conversion = new CmpJpaConversion();

        AppModule deploy = conversion.deploy(appModule);
        Assert.assertNotNull(deploy);

        List<PersistenceModule> persistenceModules = appModule.getPersistenceModules();
        Assert.assertEquals(1, persistenceModules.size());
        PersistenceModule persistenceModule = persistenceModules.get(0);
        Assert.assertEquals(appModule.getModuleUri().toString(), persistenceModule.getRootUrl());
    }


    @Test
    public void shouldReturnModuleURIAsPersistenceModuleId() throws URISyntaxException {
        String moduleURI = "getModuleUri";
        URI uri = new URI(moduleURI);
        AppModule appModule = Mockito.mock(AppModule.class);
        Mockito.when(appModule.getModuleUri()).thenReturn(uri);
        CmpJpaConversion conversion = new CmpJpaConversion();
        String persistenceModuleId = conversion.getPersistenceModuleId(appModule);
        Assert.assertEquals(moduleURI, persistenceModuleId);

    }

    @Test
    public void shouldReturngetModuleIdAsPersistenceModuleId() {
        String moduleId = "moduleId";
        AppModule appModule = Mockito.mock(AppModule.class);

        Mockito.when(appModule.getModuleId()).thenReturn(moduleId);


        CmpJpaConversion conversion = new CmpJpaConversion();
        String persistenceModuleId = conversion.getPersistenceModuleId(appModule);
        Assert.assertEquals(moduleId, persistenceModuleId);
    }



    @javax.persistence.Entity
    private static class Person {

        @Id
        private String id;
    }

    private static abstract class PersonBean implements javax.ejb.EntityBean {

    }

}