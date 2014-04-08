/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.jee.jsf;

import junit.framework.TestCase;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.JaxbJavaee;

import javax.xml.bind.JAXBElement;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.JeeTest.marshalAndUnmarshal;

/**
 * @version $Rev$ $Date$
 */
public class JsfTest  extends TestCase {

    /**
     * This test requires that there are three managed beans in faces-config.xml. It will ask JaxbJavaee to load faces-config.xml
     * and then assert if it found the three managed beans and checks if the class names are correct
     *
     * @throws Exception
     */
    public void testFacesConfig() throws Exception {
        List<String> managedBeanClasses = new ArrayList<String>();
        managedBeanClasses.add("org.apache.openejb.faces.EmployeeBean");
        managedBeanClasses.add("org.apache.openejb.faces.OneBean");
        managedBeanClasses.add("org.apache.openejb.faces.TwoBean");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jsf/faces-config.xml");
        FacesConfig facesConfig = (FacesConfig) JaxbJavaee.unmarshalJavaee(FacesConfig.class, inputStream);
        List<FacesManagedBean> managedBean = facesConfig.getManagedBean();

        for (FacesManagedBean bean : managedBean) {
            assertTrue(managedBeanClasses.contains(bean.getManagedBeanClass().trim()));
        }
        assertEquals(3, managedBean.size());

        marshalAndUnmarshal(FacesConfig.class, "jsf/faces-config.xml", null);
    }

    public void test10() throws Exception {
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_0_dtd/faces-config-simple-src.xml", "jsf/1_0_dtd/faces-config-simple-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_0_dtd/faces-config-moderate-src.xml", "jsf/1_0_dtd/faces-config-moderate-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_0_dtd/faces-config-complex-src.xml", "jsf/1_0_dtd/faces-config-complex-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_0_dtd/faces-config-empty-src.xml", "jsf/1_0_dtd/faces-config-empty-expected.xml");
    }

    public void test11() throws Exception {
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_1_dtd/faces-config-simple-src.xml", "jsf/1_0_dtd/faces-config-simple-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_1_dtd/faces-config-moderate-src.xml", "jsf/1_0_dtd/faces-config-moderate-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_1_dtd/faces-config-complex-src.xml", "jsf/1_0_dtd/faces-config-complex-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_1_dtd/faces-config-empty-src.xml", "jsf/1_0_dtd/faces-config-empty-expected.xml");
        FacesConfig f = marshalAndUnmarshal(FacesConfig.class, "jsf/1_1_dtd/faces-config.xml", null);
    }

    public void test12() throws Exception {
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_2_xsd/faces-config-simple-src.xml", "jsf/1_0_dtd/faces-config-simple-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_2_xsd/faces-config-moderate-src.xml", "jsf/1_0_dtd/faces-config-moderate-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_2_xsd/faces-config-complex-src.xml", "jsf/1_0_dtd/faces-config-complex-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/1_2_xsd/faces-config-empty-src.xml", "jsf/1_0_dtd/faces-config-empty-expected.xml");
    }

    public void testNoSchema() throws Exception {
        marshalAndUnmarshal(FacesConfig.class, "jsf/no_schema/faces-config-simple-src.xml", "jsf/1_0_dtd/faces-config-simple-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/no_schema/faces-config-moderate-src.xml", "jsf/1_0_dtd/faces-config-moderate-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/no_schema/faces-config-complex-src.xml", "jsf/1_0_dtd/faces-config-complex-expected.xml");
        marshalAndUnmarshal(FacesConfig.class, "jsf/no_schema/faces-config-empty-src.xml", "jsf/1_0_dtd/faces-config-empty-expected.xml");
    }
}
