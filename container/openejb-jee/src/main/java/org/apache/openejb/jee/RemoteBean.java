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

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public interface RemoteBean extends EnterpriseBean {

    public String getHome();

    public void setHome(String value);

    public String getRemote();

    public void setRemote(String value);

    public String getLocalHome();

    public void setLocalHome(String value);

    public String getLocal();

    public void setLocal(String value);

    public List<SecurityRoleRef> getSecurityRoleRef();

    String getBusinessLocal();

    String getBusinessRemote();
}
