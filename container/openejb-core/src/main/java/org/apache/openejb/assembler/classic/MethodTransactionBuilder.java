/*
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
package org.apache.openejb.assembler.classic;

import static org.apache.openejb.assembler.classic.MethodInfoUtil.resolveAttributes;
import static org.apache.openejb.assembler.classic.MethodInfoUtil.resolveViewAttributes;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodTransactionBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodTransactionBuilder.class);

    public void build(HashMap<String, BeanContext> deployments, List<MethodTransactionInfo> methodTransactions) throws OpenEJBException {
        for (BeanContext beanContext : deployments.values()) {
            applyTransactionAttributes(beanContext, methodTransactions);
        }
    }

    public static void applyTransactionAttributes(BeanContext beanContext, List<MethodTransactionInfo> methodTransactionInfos) throws OpenEJBException {

        if (beanContext.isBeanManagedTransaction()) return;

        methodTransactionInfos = normalize(methodTransactionInfos);

        final Map<MethodInfoUtil.ViewMethod, MethodAttributeInfo> attributes = resolveViewAttributes(methodTransactionInfos, beanContext);

        Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), MethodTransactionBuilder.class);
        final boolean debug = log.isDebugEnabled();

        for (Map.Entry<MethodInfoUtil.ViewMethod, MethodAttributeInfo> entry : attributes.entrySet()) {
            final MethodInfoUtil.ViewMethod viewMethod = entry.getKey();
            final Method method = viewMethod.getMethod();
            final String view = viewMethod.getView();

            MethodTransactionInfo transactionInfo = (MethodTransactionInfo) entry.getValue();

            if (debug) log.debug("Transaction Attribute: " + method + " -- " + MethodInfoUtil.toString(transactionInfo));

            beanContext.setMethodTransactionAttribute(method, TransactionType.get(transactionInfo.transAttribute), view);
        }
    }

    private static String getMethodInterface(MethodTransactionInfo value) {
        // We can only do this because we have previously processed all the
        // MethodTransactionInfo objects so there is one per method
        // It makes code like this easier to handle
        for (MethodInfo methodInfo : value.methods) {
            return methodInfo.methodIntf;
        }

        return null;
    }

    /**
     * This method splits the MethodTransactionInfo objects so that there is
     * exactly one MethodInfo per MethodTransactionInfo.  A single MethodTransactionInfo
     * with three MethodInfos would be expanded into three MethodTransactionInfo with
     * one MethodInfo each.
     *
     * The MethodTransactionInfo list is then sorted from least to most specific.
     *
     * @param infos
     * @return a normalized list of new MethodTransactionInfo objects
     */
    public static List<MethodTransactionInfo> normalize(List<MethodTransactionInfo> infos){
        List<MethodTransactionInfo> normalized = new ArrayList<MethodTransactionInfo>();
        for (MethodTransactionInfo oldInfo : infos) {
            for (MethodInfo methodInfo : oldInfo.methods) {
                MethodTransactionInfo newInfo = new MethodTransactionInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.transAttribute = oldInfo.transAttribute;

                normalized.add(newInfo);
            }
        }

        Collections.reverse(normalized);
        Collections.sort(normalized, new MethodTransactionComparator());

        return normalized;
    }

    public static class MethodTransactionComparator extends MethodInfoUtil.BaseComparator<MethodTransactionInfo> {
        public int compare(MethodTransactionInfo a, MethodTransactionInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }
}
