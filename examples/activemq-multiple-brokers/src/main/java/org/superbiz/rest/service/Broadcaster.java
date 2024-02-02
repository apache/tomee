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
package org.superbiz.rest.service;


import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

@Singleton
@TransactionManagement(TransactionManagementType.CONTAINER)
public class Broadcaster {

    @EJB
    private Producer producer;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void broadcastMessage(final String message) {
        try {
            final InitialContext context = new InitialContext();
            final NamingEnumeration<NameClassPair> list = context.list("openejb:Resource");

            while (list.hasMoreElements()) {
                final NameClassPair nameClassPair = list.nextElement();
                final String name = nameClassPair.getName();
                if (name.endsWith("ConnectionFactory")) {
                    producer.sendMessage(message, name.substring(0, name.length() - 17));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





}
