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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import javax.naming.NamingException;
import javax.naming.Context;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class GenericServiceLocator extends ServiceLocator {

    private final String commonPrefix;

    public GenericServiceLocator(URI serverUri, String commonPrefix) throws NamingException {
        super(serverUri);
        this.commonPrefix = commonPrefix;
    }

    public GenericServiceLocator(URI serverUri, String username, String password, String commonPrefix) throws NamingException {
        super(serverUri, username, password);
        this.commonPrefix = commonPrefix;
    }

    public GenericServiceLocator(URI serverUri, String username, String password, String realm, String commonPrefix) throws NamingException {
        super(serverUri, username, password, realm);
        this.commonPrefix = commonPrefix;
    }

    public GenericServiceLocator(Context context, String commonPrefix) {
        super(context);
        this.commonPrefix = commonPrefix;
    }

    @Override
    public Object lookup(String name) {
        if (commonPrefix != null) name = commonPrefix + "/" +name;
        return super.lookup(name);
    }

    /**
     * Usable with JNDI name formats ending in the full class name of the interface
     *
     * Such as:
     *  - {interfaceClass}
     *
     * Or with commonPrefix (supplied in constructor) such as:
     *  - {moduleId}/{interfaceClass}
     *  - ejb/{moduleId}/{interfaceClass}
     *
     * @param type the interfaceClass
     * @return (T) lookup(type.getName())
     */
    public <T> T lookup(Class<T> type) {
        return (T) lookup(type.getName());
    }

    /**
     * Usable with JNDI name formats including a varying prefix such as ejbName or deploymentID
     * and ending in the full class name of the interface
     *
     * Such as:
     *  - {ejbName}/{interfaceClass}
     *  - {deploymentId}/{interfaceClass}
     *
     * Or with commonPrefix (supplied in constructor) such as:
     *  - {moduleId}/{ejbName}/{interfaceClass}
     *  - ejb/{moduleId}/{deploymentId}/{interfaceClass}
     *
     * @param prefix such as ejbName or deploymentId
     * @param type the interfaceClass
     * @return (T) lookup(prefix + "/" + type.getName())
     */
    public <T> T lookup(String prefix, Class<T> type) {
        return (T) lookup(prefix + "/" + type.getName());
    }

    /**
     * Usable with JNDI name formats comprised of the interfaceClass and ejbClass
     *
     * For variation, the interface class is the prefix and the ejb class is the
     * suffix.  This is neat as the the prefix (the interface class name) becomes
     * a jndi context with one binding in it for each implementing ejb class.
     *
     * Works with:
     *  - {interfaceClass}/{ejbClass}
     *
     * Or with commonPrefix (supplied in constructor) such as:
     *  - {moduleId}/{interfaceClass}/{ejbClass}
     *  - ejb/{moduleId}/{interfaceClass}/{ejbClass}
     *
     * @param type the interfaceClass
     * @param ejbClass the ejbClass
     * @return (T) lookup(type.getName() + "/" + ejbClass.getName())
     */
    public <T,B> T lookup(Class<T> type, Class<B> ejbClass) {
        return (T) lookup(type.getName() + "/" + ejbClass.getName());
    }
}
