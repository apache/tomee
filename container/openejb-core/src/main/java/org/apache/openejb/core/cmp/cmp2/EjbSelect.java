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
package org.apache.openejb.core.cmp.cmp2;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.cmp.CmpContainer;

import javax.ejb.FinderException;

/**
 * DO NOT REFACTOR THIS CLASS.  This class is referenced directly by generated code.
 */
public class EjbSelect {
    public static Object execute(Object di, String methodSignature, String returnType, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        Object result = cmpContainer.select(deploymentInfo, methodSignature, returnType, args);
        if (result instanceof Number) {
            Number number = (Number) result;
            if ("char".equals(returnType) || Character.class.getName().equals(returnType)) {
                result = new Character((char) number.intValue());
            } else if ("byte".equals(returnType) || Byte.class.getName().equals(returnType)) {
                result = number.byteValue();
            } else if ("short".equals(returnType) || Short.class.getName().equals(returnType)) {
                result = number.shortValue();
            } else if ("int".equals(returnType) || Integer.class.getName().equals(returnType)) {
                result = number.intValue();
            } else if ("long".equals(returnType) || Long.class.getName().equals(returnType)) {
                result = number.longValue();
            } else if ("float".equals(returnType) || Float.class.getName().equals(returnType)) {
                result = number.floatValue();
            } else if ("double".equals(returnType) || Double.class.getName().equals(returnType)) {
                result = number.doubleValue();
            }
        }

        return result;
    }
}
