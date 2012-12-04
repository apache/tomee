/*
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
package org.apache.webbeans.util;


/**
 * Web beans related constants.
 * 
 * @version $Rev: 1363097 $ $Date: 2012-07-18 22:26:13 +0200 (mer., 18 juil. 2012) $
 */
public final class WebBeansConstants
{

    private WebBeansConstants()
    {
        throw new UnsupportedOperationException();
    }

    public static final String [] OWB_INJECTABLE_RESOURCE_ANNOTATIONS = {"javax.ejb.EJB",
                                                                         "javax.annotation.Resource",
                                                                         "javax.xml.ws.WebServiceRef",
                                                                         "javax.persistence.PersistenceUnit",
                                                                         "javax.persistence.PersistenceContext"};
    
    public static final String WEB_BEANS_XML_SPEC_SPECIFIC_INTERCEPTORS_ELEMENT = "interceptors";
    
    public static final String WEB_BEANS_XML_SPEC_SPECIFIC_DECORATORS_ELEMENT = "decorators";
    

    public static final String WEB_BEANS_XML_SPEC_SPECIFIC_ALTERNATIVES = "alternatives";
    
    public static final String WEB_BEANS_XML_SPEC_SPECIFIC_CLASS = "class";
    
    public static final String WEB_BEANS_XML_SPEC_SPECIFIC_STEREOTYPE = "stereotype";
    
    public static final String WEB_BEANS_XML_OWB_SPECIFIC_CLASS = "Class";
    
    public static final String WEB_BEANS_XML_OWB_SPECIFIC_STEREOTYPE = "Stereotype";
    
    /**JNDI name of the {@link javax.enterprise.inject.spi.BeanManager} instance*/
    public static final String WEB_BEANS_MANAGER_JNDI_NAME = "java:comp/BeanManager";

}
