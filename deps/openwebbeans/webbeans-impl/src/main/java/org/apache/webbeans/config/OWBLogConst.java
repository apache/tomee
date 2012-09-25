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
package org.apache.webbeans.config;

/* Requires the following import where referenced:
 * import org.apache.webbeans.config.OWBLogConst;
 */

public class OWBLogConst
{
    private OWBLogConst()
    {
        // utility class doesn't have a public ct
    }

    public static final String TEXT_MB_IMPL          = "TEXT_MB_IMPL";          // Managed Bean implementation class :
    public static final String TEXT_SAME_SCOPE       = "TEXT_SAME_SCOPE";       //  stereotypes must declare the same @Scope annotations.

    public static final String INFO_0001 = "INFO_0001"; // OpenWebBeans Container has started, it took {0} ms.
    public static final String INFO_0002 = "INFO_0002"; // OpenWebBeans Container has stopped for context path, 
    public static final String INFO_0003 = "INFO_0003"; // All injection points are validated successfully.
    public static final String INFO_0004 = "INFO_0004"; // Adding OpenWebBeansPlugin \:
    public static final String INFO_0005 = "INFO_0005"; // OpenWebBeans Container is starting...
    public static final String INFO_0006 = "INFO_0006"; // Initializing OpenWebBeans Container.
    public static final String INFO_0008 = "INFO_0008"; // Stopping OpenWebBeans Container...
    public static final String INFO_0009 = "INFO_0009"; // OpenWebBeans Container has stopped.
    public static final String INFO_0010 = "INFO_0010"; // Cannot send event to bean in non-active context \: [{0}]
    public static final String INFO_0011 = "INFO_0011"; // Conversation with id {0} has been destroyed because of inactive time period.


    public static final String WARN_0001 = "WARN_0001"; // No plugins to shutDown.
    public static final String WARN_0002 = "WARN_0002"; // Alternative XML content is wrong. Child of <alternatives> must be <class>,<stereotype> but found : 
    public static final String WARN_0003 = "WARN_0003"; // Conversation already started with cid : [{0}]
    public static final String WARN_0004 = "WARN_0004"; // Conversation already ended with cid \: [{0}]

    // [{0}] has not DependentScope. If an interceptor or decorator has any scope other than @Dependent, non-portable behaviour results.
    public static final String WARN_0005_1 = "WARN_0005_1";

    public static final String WARN_0005_2 = "WARN_0005_2"; // [{0}] has a name. If an interceptor or decorator has name, non-portable behaviour results.
    public static final String WARN_0005_3 = "WARN_0005_3"; // [{0}] is Alternative. If an interceptor or decorator is @Alternative, non-portable behaviour results.
    public static final String WARN_0006 = "WARN_0006"; // Unable to close entity manager factory with name \: [{0}]
    public static final String WARN_0007 = "WARN_0007"; // Exception in ejbContext.proceed().
    public static final String WARN_0008 = "WARN_0008"; // Unable to find EJB bean with class \: [{0}] \: [{1}]
    public static final String WARN_0009 = "WARN_0009"; // Unable to find service with class name \: [{0}]
    public static final String WARN_0010 = "WARN_0010"; // Trying to serialize non-passivation capable bean proxy \: [{0}]
    public static final String WARN_0011 = "WARN_0011"; // Trying to de-serialize non-passivation capable bean proxy \: [{0}]
    public static final String WARN_0012 = "WARN_0012"; // No suitable constructor found for injection target class \: [{0}]. produce() method does not work!
    public static final String WARN_0013 = "WARN_0013"; // Unable to clear ResourceFactory.
    public static final String WARN_0014 = "WARN_0014"; // Could not find {0} with name [{1}].
    public static final String WARN_0015 = "WARN_0015"; // Trying to serialize non-passivation capable bean proxy \: [{0}]
    public static final String WARN_0016 = "WARN_0016"; // Stereotypes can not annotated with @Typed but stereotype \: [{0}] has annotated, non-portable behaviour results.

    // Stereotypes can not define qualifier other than @Named but stereotype \: [{0}] has defined [{1}] , non-portable behaviour results.
    public static final String WARN_0017 = "WARN_0017";
    public static final String WARN_0018 = "WARN_0018";


    public static final String ERROR_0001 = "ERROR_0001"; // Unable to inject resource for : [{0}]
    public static final String ERROR_0002 = "ERROR_0002"; // Initialization of the WebBeans container has failed.
    public static final String ERROR_0003 = "ERROR_0003"; // An exception occurred in the transactional observer.
    public static final String ERROR_0004 = "ERROR_0004"; // Unable to initialize InitialContext object.
    public static final String ERROR_0005 = "ERROR_0005"; // Unable to bind object with name : [{0}]
    public static final String ERROR_0006 = "ERROR_0006"; // Security exception. Cannot access decorator class: [{0}] method : [{1}]
    public static final String ERROR_0007 = "ERROR_0007"; // Delegate field is not found on the given decorator class : [{0}]
    public static final String ERROR_0008 = "ERROR_0008"; // An error occurred while executing {0}
    public static final String ERROR_0009 = "ERROR_0009"; // An error occurred while shutting down the plugin : [{0}]
    public static final String ERROR_0010 = "ERROR_0010"; // An error occurred while closing the JMS instance.
    public static final String ERROR_0011 = "ERROR_0011"; // Method security access violation for method : [{0}] in decorator class : [{1}]
    public static final String ERROR_0012 = "ERROR_0012"; // Exception in calling method : [{0}] in decorator class : [{1}]. Look in the log for target checked exception.
    public static final String ERROR_0013 = "ERROR_0013"; // An Exception occurred while starting a fresh session!
    public static final String ERROR_0014 = "ERROR_0014"; // Method illegal access for method : [{0}] in decorator class : [{1}]
    public static final String ERROR_0015 = "ERROR_0015"; // Illegal access exception for field : [{0}] in decorator class : [{1}]
    public static final String ERROR_0016 = "ERROR_0016"; // IllegalArgumentException has occurred while calling the field: [{0}] on the class: [{1}]
    public static final String ERROR_0017 = "ERROR_0017"; // IllegalAccessException has occurred while calling the field: [{0}] on the class: [{1}]
    public static final String ERROR_0018 = "ERROR_0018"; // An error occured while starting application context path \: [{0}]
    public static final String ERROR_0019 = "ERROR_0019"; // An error occured while starting request \: [{0}]
    public static final String ERROR_0020 = "ERROR_0020"; // An error occured while starting session \: [{0}]
    public static final String ERROR_0021 = "ERROR_0021"; // An error occured while stopping the container.
    public static final String ERROR_0022 = "ERROR_0022"; // Unable to inject dependencies of EJB interceptor instance with class \: [{0}]
    public static final String ERROR_0023 = "ERROR_0023"; // An error occured while injecting Java EE Resources for the bean instance \: [{0}]
    public static final String ERROR_0024 = "ERROR_0024"; // Unable to get resource with class \: [{0}] in \: [{1}] with name \: [{2}].
    public static final String ERROR_0025 = "ERROR_0025"; // Unable to inject field \: [{0}]
    public static final String ERROR_0026 = "ERROR_0026"; // An error occured while injecting dependencies of bean \: [{0}]
    public static final String ERROR_0027 = "ERROR_0027"; // Unable to create AnnotatedType for class \: [{0}]. Exception cause \: [{1}].
    public static final String ERROR_0028 = "ERROR_0028"; // Can't use logger factory class [{0}]. Exception cause \: [{1}].


    public static final String FATAL_0001 = "FATAL_0001"; // Exception thrown while destroying bean instance : {0}
    public static final String FATAL_0002 = "FATAL_0002"; // Unable to read root element of the given input stream.

    public static final String EDCONF_FAIL = "CRITICAL_DEFAULT_CONFIG_FAILURE"; // Problem while loading OpenWebBeans default configuration.
    public static final String EXCEPT_0002 = "EXCEPT_0002"; // Wrong ended object.
    public static final String EXCEPT_0003 = "EXCEPT_0003"; // Specialized class [
    public static final String EXCEPT_0004 = "EXCEPT_0004"; // ] must extend another class.
    public static final String EXCEPT_XML  = "EXCEPT_XML";  // XML Specialization Error : 
    public static final String EXCEPT_0005 = "EXCEPT_0005"; // More than one class specialized the same super class :
    public static final String EXCEPT_0006 = "EXCEPT_0006"; // Got Exceptions while sending shutdown to the following plugins : {0} 
    public static final String EXCEPT_0007 = "EXCEPT_0007"; // TransactionPhase not supported: 
    public static final String EXCEPT_0008 = "EXCEPT_0008"; // Exception is thrown while handling event object with type : 
    public static final String EXCEPT_0009 = "EXCEPT_0009"; // Unable to unbind object with name : 
    public static final String EXCEPT_0010 = "EXCEPT_0010"; // Unable to lookup object with name : 
    public static final String EXCEPT_0012 = "EXCEPT_0012"; // All elements in the beans.xml file have to declare name space.
    public static final String EXCEPT_0013 = "EXCEPT_0013"; // Unable to read root element of the given input stream.
    public static final String EXCEPT_0014 = "EXCEPT_0014"; // Multiple class with name : 
    public static final String EXCEPT_0015 = "EXCEPT_0015"; // Passivation bean \: [{0}] decorators must be passivating capable.
    public static final String EXCEPT_0016 = "EXCEPT_0016"; // Passivation bean \: [{0}] interceptors must be passivating capable.
    public static final String EXCEPT_0017 = "EXCEPT_0017"; // Passivation bean \: [{0}] interceptor \: [{1}] must have serializable injection points.
}
