/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The interceptor-bindingType element describes the binding of
 * interceptor classes to beans within the ejb-jar.
 * It consists of :
 * <p/>
 * - An optional description.
 * - The name of an ejb within the ejb-jar or the wildcard value "*",
 * which is used to define interceptors that are bound to all
 * beans in the ejb-jar.
 * - A list of interceptor classes that are bound to the contents of
 * the ejb-name element or a specification of the total ordering
 * over the interceptors defined for the given level and above.
 * - An optional exclude-default-interceptors element.  If set to true,
 * specifies that default interceptors are not to be applied to
 * a bean-class and/or business method.
 * - An optional exclude-class-interceptors element.  If set to true,
 * specifies that class interceptors are not to be applied to
 * a business method.
 * - An optional set of method elements for describing the name/params
 * of a method-level interceptor.
 * <p/>
 * Interceptors bound to all classes using the wildcard syntax
 * "*" are default interceptors for the components in the ejb-jar.
 * In addition, interceptors may be bound at the level of the bean
 * class (class-level interceptors) or business methods (method-level
 * interceptors ).
 * <p/>
 * The binding of interceptors to classes is additive.  If interceptors
 * are bound at the class-level and/or default-level as well as the
 * method-level, both class-level and/or default-level as well as
 * method-level will apply.
 * <p/>
 * There are four possible styles of the interceptor element syntax :
 * <p/>
 * 1.
 * <p/>
 * Specifying the ejb-name as the wildcard value "*" designates
 * default interceptors (interceptors that apply to all session and
 * message-driven beans contained in the ejb-jar).
 * <p/>
 * 2.
 * <p/>
 * This style is used to refer to interceptors associated with the
 * specified enterprise bean(class-level interceptors).
 * <p/>
 * 3.
 * <p/>
 * This style is used to associate a method-level interceptor with
 * the specified enterprise bean.  If there are multiple methods
 * with the same overloaded name, the element of this style refers
 * to all the methods with the overloaded name.  Method-level
 * interceptors can only be associated with business methods of the
 * bean class.   Note that the wildcard value "*" cannot be used
 * to specify method-level interceptors.
 * <p/>
 * 4.
 * <p/>
 * ...
 * This style is used to associate a method-level interceptor with
 * the specified method of the specified enterprise bean.  This
 * style is used to refer to a single method within a set of methods
 * with an overloaded name.  The values PARAM-1 through PARAM-N
 * are the fully-qualified Java types of the method's input parameters
 * (if the method has no input arguments, the method-params element
 * contains no method-param elements). Arrays are specified by the
 * array element's type, followed by one or more pair of square
 * brackets (e.g. int[][]).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptor-bindingType", propOrder = {
        "description",
        "ejbName",
        "interceptorClass",
        "interceptorOrder",
        "excludeDefaultInterceptors",
        "excludeClassInterceptors",
        "method"
        })
public class InterceptorBinding {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "interceptor-class", required = true)
    protected List<String> interceptorClass;
    @XmlElement(name = "interceptor-order")
    protected InterceptorOrder interceptorOrder;
    @XmlElement(name = "exclude-default-interceptors")
    protected boolean excludeDefaultInterceptors;
    @XmlElement(name = "exclude-class-interceptors")
    protected boolean excludeClassInterceptors;
    protected NamedMethod method;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public List<String> getInterceptorClass() {
        if (interceptorClass == null) {
            interceptorClass = new ArrayList<String>();
        }
        return this.interceptorClass;
    }

    public InterceptorOrder getInterceptorOrder() {
        return interceptorOrder;
    }

    public void setInterceptorOrder(InterceptorOrder value) {
        this.interceptorOrder = value;
    }

    public boolean getExcludeDefaultInterceptors() {
        return excludeDefaultInterceptors;
    }

    public void setExcludeDefaultInterceptors(boolean value) {
        this.excludeDefaultInterceptors = value;
    }

    public boolean getExcludeClassInterceptors() {
        return excludeClassInterceptors;
    }

    public void setExcludeClassInterceptors(boolean value) {
        this.excludeClassInterceptors = value;
    }

    public NamedMethod getMethod() {
        return method;
    }

    public void setMethod(NamedMethod value) {
        this.method = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
