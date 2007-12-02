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
package org.apache.openejb.webadmin;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface HttpSession {

    public void removeAttribute(String name);

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name a string specifying the name of the object
     *
     * @return the object with the specified name
     */
    public Object getAttribute(String name);

    /**
     * Binds an object to this session, using the name specified. If an object
     * of the same name is already bound to the session, the object is
     * replaced.
     *
     * @param name the name to which the object is bound; cannot be null
     * @param value the object to be bound
     */
    public void setAttribute(String name, Object value);

    /**
     * Returns a string containing the unique identifier assigned to this
     * session. The identifier is assigned by the ejb container and is
     * implementation dependent.
     *
     * @return a string specifying the identifier assigned to this session
     */
    public String getId();
}


