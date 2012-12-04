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
package org.apache.webbeans.component;


/**
 * There are different <i>Web Beans Component</i> with regard to the definition.
 * These are the;
 * <ul>
 * <li>Bean Implementation Class Components</li>
 * <li>Producer Method Components</li>
 * </ul>
 * <p>
 * Bean Implementation Class Components are defined with annotating the ordinary
 * classes with {@link OwbBean} annotation. It maybe defined within the
 * web-beans.xml file using &lt;class&gt; element. It is possible to mix these
 * two definitions.
 * </p>
 * <p>
 * Producer Method Components are defined within the class that is annotated
 * with {@link OwbBean} annotation. In these classes, there are methods that
 * are annotated with {@link javax.enterprise.inject.Produces} annotation.
 * These methods become the producer method components of this web beans component.
 * It maybe defined using the web-beans.xml file using the &lt;producer&gt; element.
 * It is possible to mix these two definitons.
 * </p>
 * <p>
 * For further details about the components, see Web Beans Specification
 * Chapter-2.
 * </p>
 * 
 * @version $Rev: 952420 $ $Date: 2010-06-07 22:35:47 +0200 (lun., 07 juin 2010) $
 */
public enum WebBeansType
{
    MANAGED, 
    PRODUCERMETHOD, 
    PRODUCERFIELD,
    RESOURCEBEAN,
    NEW, 
    ENTERPRISE, 
    JMS, 
    DEPENDENT, 
    INTERCEPTOR, 
    DECORATOR, 
    OBSERVABLE, 
    MANAGER, 
    CONVERSATION,
    INSTANCE,
    INJECTIONPOINT,
    THIRDPARTY,
    EXTENSION,
    USERTRANSACTION,
    PRINCIPAL,
    VALIDATIONFACT,
    VALIDATION
}