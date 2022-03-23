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

package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.loader.SystemInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CheckIncorrectPropertyNames extends ValidationBase {

    Map incorrectAndCorrectPropNames = new HashMap<String, String>();

    {
        //incorrect property key : correct property key
        incorrectAndCorrectPropNames.put("java.persistence.provider", "jakarta.persistence.provider");
        incorrectAndCorrectPropNames.put("java.persistence.transactionType", "jakarta.persistence.transactionType");
        incorrectAndCorrectPropNames.put("java.persistence.jtaDataSource", "jakarta.persistence.jtaDataSource");
        incorrectAndCorrectPropNames.put("java.persistence.nonJtaDataSource", "jakarta.persistence.nonJtaDataSource");
        incorrectAndCorrectPropNames.put("java.net.ssl.keyStore", "javax.net.ssl.keyStore");
        incorrectAndCorrectPropNames.put("java.net.ssl.keyStorePassword", "javax.net.ssl.keyStorePassword");
        incorrectAndCorrectPropNames.put("java.net.ssl.trustStore", "javax.net.ssl.trustStore");
        incorrectAndCorrectPropNames.put("java.security.jacc.PolicyConfigurationFactory.provider", "jakarta.security.jacc.PolicyConfigurationFactory.provider");
        incorrectAndCorrectPropNames.put("java.security.jacc.policy.provider", "jakarta.security.jacc.policy.provider");
        incorrectAndCorrectPropNames.put("java.xml.ws.spi.Provider", "jakarta.xml.ws.spi.Provider");
        incorrectAndCorrectPropNames.put("java.xml.soap.MessageFactory", "jakarta.xml.soap.MessageFactory");
        incorrectAndCorrectPropNames.put("java.xml.soap.SOAPFactory", "jakarta.xml.soap.SOAPFactory");
        incorrectAndCorrectPropNames.put("java.xml.soap.SOAPConnectionFactory", "jakarta.xml.soap.SOAPConnectionFactory");
        incorrectAndCorrectPropNames.put("java.xml.soap.MetaFactory", "jakarta.xml.soap.MetaFactory");
        incorrectAndCorrectPropNames.put("java.persistence.sharedCache.mode", "jakarta.persistence.sharedCache.mode");
        incorrectAndCorrectPropNames.put("java.persistence.validation.mode", "jakarta.persistence.validation.mode");
        incorrectAndCorrectPropNames.put("java.persistence.transactionType", "jakarta.persistence.transactionType");

        incorrectAndCorrectPropNames.put("javax.naming.applet", "java.naming.applet");
        incorrectAndCorrectPropNames.put("javax.naming.authoritative", "java.naming.authoritative");
        incorrectAndCorrectPropNames.put("javax.naming.batchsize", "java.naming.batchsize");
        incorrectAndCorrectPropNames.put("javax.naming.dns.url", "java.naming.dns.url");
        incorrectAndCorrectPropNames.put("javax.naming.factory.initial", "java.naming.factory.initial");
        incorrectAndCorrectPropNames.put("javax.naming.factory.object", "java.naming.factory.object");
        incorrectAndCorrectPropNames.put("javax.naming.factory.state", "java.naming.factory.state");
        incorrectAndCorrectPropNames.put("javax.naming.factory.url.pkgs", "java.naming.factory.url.pkgs");
        incorrectAndCorrectPropNames.put("javax.naming.language", "java.naming.language");
        incorrectAndCorrectPropNames.put("javax.naming.provider.url", "java.naming.provider.url");
        incorrectAndCorrectPropNames.put("javax.naming.referral", "java.naming.referral");
        incorrectAndCorrectPropNames.put("javax.naming.security.authentication", "java.naming.security.authentication");
        incorrectAndCorrectPropNames.put("javax.naming.security.credentials", "java.naming.security.credentials");
        incorrectAndCorrectPropNames.put("javax.naming.security.principal", "java.naming.security.principal");
        incorrectAndCorrectPropNames.put("javax.naming.security.protocol", "java.naming.security.protocol");

    }

    @Override
    public void validate(final AppModule appModule) {
        this.module = appModule;
        final Properties systemProperties = SystemInstance.get().getProperties();

        for (Object o : incorrectAndCorrectPropNames.entrySet()) {
            final Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
            if (systemProperties.containsKey(entry.getKey())) {
                warn(appModule.toString(), "incorrect.property.name", entry.getKey(), entry.getValue());
            }
        }

    }
}
