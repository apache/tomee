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

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.assembler.classic.MethodInfoUtil.resolveViewAttributes;

/**
 * @version $Rev$ $Date$
 */
public class MethodTransactionBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodTransactionBuilder.class);

    public void build(final HashMap<String, BeanContext> deployments, final List<MethodTransactionInfo> methodTransactions) throws OpenEJBException {
        for (final BeanContext beanContext : deployments.values()) {
            applyTransactionAttributes(beanContext, methodTransactions);
        }
    }

    public static void applyTransactionAttributes(final BeanContext beanContext, List<MethodTransactionInfo> methodTransactionInfos) throws OpenEJBException {

        if (beanContext.isBeanManagedTransaction()) {
            return;
        }

        methodTransactionInfos = normalize(methodTransactionInfos);

        final Map<MethodInfoUtil.ViewMethod, MethodAttributeInfo> attributes = resolveViewAttributes(methodTransactionInfos, beanContext);

        final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), MethodTransactionBuilder.class);
        final boolean debug = log.isDebugEnabled();

        for (final Map.Entry<MethodInfoUtil.ViewMethod, MethodAttributeInfo> entry : attributes.entrySet()) {
            final MethodInfoUtil.ViewMethod viewMethod = entry.getKey();
            final Method method = viewMethod.getMethod();
            final String view = viewMethod.getView();

            final MethodTransactionInfo transactionInfo = (MethodTransactionInfo) entry.getValue();

            if (debug) {
                log.debug("Transaction Attribute: " + method + " -- " + MethodInfoUtil.toString(transactionInfo));
            }

            beanContext.setMethodTransactionAttribute(method, TransactionType.get(transactionInfo.transAttribute), view);
        }
    }

    /**
     * This method splits the MethodTransactionInfo objects so that there is
     * exactly one MethodInfo per MethodTransactionInfo.  A single MethodTransactionInfo
     * with three MethodInfos would be expanded into three MethodTransactionInfo with
     * one MethodInfo each.
     * <p/>
     * The MethodTransactionInfo list is then sorted from least to most specific.
     *
     * @return a normalized list of new MethodTransactionInfo objects
     */
    public static List<MethodTransactionInfo> normalize(final List<MethodTransactionInfo> infos) {
        final List<MethodTransactionInfo> normalized = new ArrayList<>();
        for (final MethodTransactionInfo oldInfo : infos) {
            for (final MethodInfo methodInfo : oldInfo.methods) {
                final MethodTransactionInfo newInfo = new MethodTransactionInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.transAttribute = oldInfo.transAttribute;

                normalized.add(newInfo);
            }
        }

        Collections.reverse(normalized);
        normalized.sort(new MethodTransactionComparator());

        return normalized;
    }

    public static class MethodTransactionComparator extends MethodInfoUtil.BaseComparator<MethodTransactionInfo> {
        public int compare(final MethodTransactionInfo a, final MethodTransactionInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }
}
