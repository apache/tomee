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
package org.apache.openejb.core.ivm.naming;

import javax.naming.NamingException;


/**
 * This interface is implemented by special wrappers for EJB references and
 * resource references. When the getObject( ) method is invoked the Operation
 * is checked to ensure that its is allowed for the bean's current state.
 * <p/>
 * In addition, dynamic resolution and special conditions can be encapsulated
 * in the implementation object.
 *
 * @version $Rev$ $Date$
 */
public interface Reference {

    public Object getObject() throws NamingException;
}