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

import javax.xml.bind.annotation.XmlEnumValue;


/**
 * The res-authType specifies whether the Deployment Component
 * code signs on programmatically to the resource manager, or
 * whether the Container will sign on to the resource manager
 * on behalf of the Deployment Component. In the latter case,
 * the Container uses information that is supplied by the
 * Deployer.
 * <p/>
 * The value must be one of the two following:
 * <p/>
 * Application
 * Container
 */
public enum ResAuth {
    @XmlEnumValue("Application") APPLICATION,
    @XmlEnumValue("Container") CONTAINER;
}
