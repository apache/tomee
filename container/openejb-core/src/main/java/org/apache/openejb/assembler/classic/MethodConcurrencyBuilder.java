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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;

import javax.ejb.LockType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodConcurrencyBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodConcurrencyBuilder.class);

    public void build(HashMap<String, DeploymentInfo> deployments, List<MethodConcurrencyInfo> methodConcurrencys) throws OpenEJBException {
        for (DeploymentInfo deploymentInfo : deployments.values()) {
            MethodConcurrencyBuilder.applyConcurrencyAttributes((CoreDeploymentInfo) deploymentInfo, methodConcurrencys);
        }
    }

    public static void applyConcurrencyAttributes(CoreDeploymentInfo deploymentInfo, List<MethodConcurrencyInfo> methodConcurrencyInfos) throws OpenEJBException {

        if (deploymentInfo.isBeanManagedConcurrency()) return;

        methodConcurrencyInfos = MethodConcurrencyBuilder.normalize(methodConcurrencyInfos);

        Map<Method, MethodAttributeInfo> attributes = MethodInfoUtil.resolveAttributes(methodConcurrencyInfos, deploymentInfo);

        Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), MethodConcurrencyBuilder.class);
        if (log.isDebugEnabled()) {
            for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                Method method = entry.getKey();
                MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
                log.debug("Concurrency Attribute: " + method + " -- " + MethodInfoUtil.toString(value));
            }
        }

        for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
            MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();

//            logger.info(entry.getKey().toString() +"  "+ value.transAttribute);
            String s = value.concurrencyAttribute.toUpperCase();
            deploymentInfo.setMethodConcurrencyAttribute(entry.getKey(), LockType.valueOf(s));
        }
    }

    /**
     * This method splits the MethodConcurrencyInfo objects so that there is
     * exactly one MethodInfo per MethodConcurrencyInfo.  A single MethodConcurrencyInfo
     * with three MethodInfos would be expanded into three MethodConcurrencyInfo with
     * one MethodInfo each.
     *
     * The MethodConcurrencyInfo list is then sorted from least to most specific.
     *
     * @param infos
     * @return a normalized list of new MethodConcurrencyInfo objects
     */
    public static List<MethodConcurrencyInfo> normalize(List<MethodConcurrencyInfo> infos){
        List<MethodConcurrencyInfo> normalized = new ArrayList<MethodConcurrencyInfo>();
        for (MethodConcurrencyInfo oldInfo : infos) {
            for (MethodInfo methodInfo : oldInfo.methods) {
                MethodConcurrencyInfo newInfo = new MethodConcurrencyInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.concurrencyAttribute = oldInfo.concurrencyAttribute;

                normalized.add(newInfo);
            }
        }

        Collections.reverse(normalized);
        Collections.sort(normalized, new MethodConcurrencyBuilder.MethodConcurrencyComparator());

        return normalized;
    }

    public static class MethodConcurrencyComparator extends MethodInfoUtil.BaseComparator<MethodConcurrencyInfo> {
        public int compare(MethodConcurrencyInfo a, MethodConcurrencyInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }
}
