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
package org.apache.openejb.util;
/**
 * Contains Logger categories used in OpenEJB. Be careful when adding new Categories. For example, if a new Category
 * named OpenEJB.shutdown needs to be added, then the following is not a recommended way
 * String OPENEJB_SHUTDOWN = "OpenEJB.shutdown";
 * The above is not recommended because the above logger has a parent logger in OpenEJB. If we change the Parent logger
 * category i.e. lets say to OPENEJB (all uppercase), then to maintain the parent-child relationship, we will need
 * to change other loggers too. In our case, we will not need to change OPENEJB_STARTUP and OPENEJB_SERVER because 
 * of the way they are defined.
 * A better way of adding the Category would be
 * String OPENEJB_SHUTDOWN = OPENEJB+".shutdown";
 * 
 *
 */
public interface LogCategory {
	String OPENEJB = "OpenEJB";
	String OPENEJB_STARTUP = OPENEJB + ".startup";
	String OPENEJB_STARTUP_CONFIG = OPENEJB_STARTUP+".config";
	String OPENEJB_STARTUP_VALIDATION = OPENEJB_STARTUP+".validation";
	String OPENEJB_SERVER = OPENEJB + ".server";
	String OPENEJB_SECURITY = OPENEJB+".security";
	String OPENEJB_RESOURCE_JDBC = OPENEJB+".resource.jdbc";
	String OPENEJB_CONNECTOR = OPENEJB+".connector";
	String OPENEJB_DEPLOY = OPENEJB+".deploy";
	String TRANSACTION = "Transaction";
	String ACTIVEMQ = "org.apache.activemq";
	String GERONIMO = "org.apache.geronimo";
	String OPENJPA = "openjpa";
	String CORBA_ADAPTER = "CORBA-Adapter";
	String TIMER = "Timer";

}
