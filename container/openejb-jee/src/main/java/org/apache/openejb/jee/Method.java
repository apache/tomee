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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The methodType is used to denote a method of an enterprise
 * bean's business, home, component, and/or web service endpoint
 * interface, or, in the case of a message-driven bean, the
 * bean's message listener method, or a set of such
 * methods. The ejb-name element must be the name of one of the
 * enterprise beans declared in the deployment descriptor; the
 * optional method-intf element allows to distinguish between a
 * method with the same signature that is multiply defined
 * across the business, home, component, and/or web service
 * endpoint nterfaces; the method-name element specifies the
 * method name; and the optional method-params elements identify
 * a single method among multiple methods with an overloaded
 * method name.
 * <p/>
 * There are three possible styles of using methodType element
 * within a method element:
 * <p/>
 * 1.
 * <method>
 * <ejb-name>EJBNAME</ejb-name>
 * <method-name>*</method-name>
 * </method>
 * <p/>
 * This style is used to refer to all the methods of the
 * specified enterprise bean's business, home, component,
 * and/or web service endpoint interfaces.
 * <p/>
 * 2.
 * <method>
 * <ejb-name>EJBNAME</ejb-name>
 * <method-name>METHOD</method-name>
 * </method>
 * <p/>
 * This style is used to refer to the specified method of
 * the specified enterprise bean. If there are multiple
 * methods with the same overloaded name, the element of
 * this style refers to all the methods with the overloaded
 * name.
 * <p/>
 * 3.
 * <method>
 * <ejb-name>EJBNAME</ejb-name>
 * <method-name>METHOD</method-name>
 * <method-params>
 * <method-param>PARAM-1</method-param>
 * <method-param>PARAM-2</method-param>
 * ...
 * <method-param>PARAM-n</method-param>
 * </method-params>
 * </method>
 * <p/>
 * This style is used to refer to a single method within a
 * set of methods with an overloaded name. PARAM-1 through
 * PARAM-n are the fully-qualified Java types of the
 * method's input parameters (if the method has no input
 * arguments, the method-params element contains no
 * method-param elements). Arrays are specified by the
 * array element's type, followed by one or more pair of
 * square brackets (e.g. int[][]). If there are multiple
 * methods with the same overloaded name, this style refers
 * to all of the overloaded methods.
 * <p/>
 * Examples:
 * <p/>
 * Style 1: The following method element refers to all the
 * methods of the EmployeeService bean's business, home,
 * component, and/or web service endpoint interfaces:
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-name>*</method-name>
 * </method>
 * <p/>
 * Style 2: The following method element refers to all the
 * create methods of the EmployeeService bean's home
 * interface(s).
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-name>create</method-name>
 * </method>
 * <p/>
 * Style 3: The following method element refers to the
 * create(String firstName, String LastName) method of the
 * EmployeeService bean's home interface(s).
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-name>create</method-name>
 * <method-params>
 * <method-param>String</method-param>
 * <method-param>String</method-param>
 * </method-params>
 * </method>
 * <p/>
 * The following example illustrates a Style 3 element with
 * more complex parameter types. The method
 * foobar(char s, int i, int[] iar, mypackage.MyClass mycl,
 * mypackage.MyClass[][] myclaar) would be specified as:
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-name>foobar</method-name>
 * <method-params>
 * <method-param>char</method-param>
 * <method-param>int</method-param>
 * <method-param>int[]</method-param>
 * <method-param>mypackage.MyClass</method-param>
 * <method-param>mypackage.MyClass[][]</method-param>
 * </method-params>
 * </method>
 * <p/>
 * The optional method-intf element can be used when it becomes
 * necessary to differentiate between a method that is multiply
 * defined across the enterprise bean's business, home, component,
 * and/or web service endpoint interfaces with the same name and
 * signature. However, if the same method is a method of both the
 * local business interface, and the local component interface,
 * the same attribute applies to the method for both interfaces.
 * Likewise, if the same method is a method of both the remote
 * business interface and the remote component interface, the same
 * attribute applies to the method for both interfaces.
 * <p/>
 * For example, the method element
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-intf>Remote</method-intf>
 * <method-name>create</method-name>
 * <method-params>
 * <method-param>String</method-param>
 * <method-param>String</method-param>
 * </method-params>
 * </method>
 * <p/>
 * can be used to differentiate the create(String, String)
 * method defined in the remote interface from the
 * create(String, String) method defined in the remote home
 * interface, which would be defined as
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-intf>Home</method-intf>
 * <method-name>create</method-name>
 * <method-params>
 * <method-param>String</method-param>
 * <method-param>String</method-param>
 * </method-params>
 * </method>
 * <p/>
 * and the create method that is defined in the local home
 * interface which would be defined as
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-intf>LocalHome</method-intf>
 * <method-name>create</method-name>
 * <method-params>
 * <method-param>String</method-param>
 * <method-param>String</method-param>
 * </method-params>
 * </method>
 * <p/>
 * The method-intf element can be used with all three Styles
 * of the method element usage. For example, the following
 * method element example could be used to refer to all the
 * methods of the EmployeeService bean's remote home interface
 * and the remote business interface.
 * <p/>
 * <method>
 * <ejb-name>EmployeeService</ejb-name>
 * <method-intf>Home</method-intf>
 * <method-name>*</method-name>
 * </method>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "methodType", propOrder = {
        "descriptions",
        "ejbName",
        "methodIntf",
        "methodName",
        "methodParams"
        })
public class Method {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlTransient
    protected String className;

    @XmlElement(name = "method-intf")
    protected MethodIntf methodIntf;
    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected TextMap description = new TextMap();

    public Method(String ejbName, java.lang.reflect.Method method) {
        this.ejbName = ejbName;
        this.methodName = method.getName();
        MethodParams methodParams = new MethodParams();
        for (Class<?> type : method.getParameterTypes()) {
            methodParams.getMethodParam().add(type.getCanonicalName());
        }
        this.methodParams = methodParams;
    }

    public Method(String ejbName, String methodName, String... parameters) {
        this(ejbName, null, methodName, parameters);
    }

    public Method(String ejbName, String className, String methodName, String... parameters) {
        this.ejbName = ejbName;
        this.methodName = methodName;
        this.className = className;

        if (parameters.length > 0){
            MethodParams params = new MethodParams();
            for (String paramName : parameters) {
                params.getMethodParam().add(paramName);
            }
            this.methodParams = params;
        }
    }

    public Method() {
    }

    public Method(String ejbName, String methodName) {
        this(ejbName, null, methodName);
    }

    public Method(String ejbName, String className, String methodName) {
        this.ejbName = ejbName;
        this.methodName = methodName;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getEjbName() {
        return ejbName;
    }

    /**
     * The ejb-nameType specifies an enterprise bean's name. It is
     * used by ejb-name elements. This name is assigned by the
     * ejb-jar file producer to name the enterprise bean in the
     * ejb-jar file's deployment descriptor. The name must be
     * unique among the names of the enterprise beans in the same
     * ejb-jar file.
     * <p/>
     * There is no architected relationship between the used
     * ejb-name in the deployment descriptor and the JNDI name that
     * the Deployer will assign to the enterprise bean's home.
     * <p/>
     * The name for an entity bean must conform to the lexical
     * rules for an NMTOKEN.
     * <p/>
     * Example:
     * <p/>
     * <ejb-name>EmployeeService</ejb-name>
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public MethodIntf getMethodIntf() {
        return methodIntf;
    }

    public void setMethodIntf(MethodIntf value) {
        this.methodIntf = value;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * contains a name of an enterprise
     * bean method or the asterisk (*) character. The asterisk is
     * used when the element denotes all the methods of an
     * enterprise bean's client view interfaces.
     */
    public void setMethodName(String value) {
        this.methodName = value;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(MethodParams value) {
        this.methodParams = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
