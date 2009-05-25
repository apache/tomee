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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.ejb.embeddable;

import java.util.Collections;

public abstract class EJBContainer {

    public static final String PROVIDER = "javax.ejb.embeddable.provider";
    public static final String APP_NAME = "javax.ejb.embeddable.appName";
    public static final String MODULES = "javax.ejb.embeddable.modules";

    public EJBContainer() {
    }

    public abstract void close();

    public static EJBContainer createEJBContainer() {
        return createEJBContainer(Collections.EMPTY_MAP);
    }

    public static EJBContainer createEJBContainer(java.util.Map<?, ?> properties) {
        return null; // TODO
    }

    public abstract javax.naming.Context getContext();

}
