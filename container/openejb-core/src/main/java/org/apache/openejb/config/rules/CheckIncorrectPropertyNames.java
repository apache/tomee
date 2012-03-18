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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.loader.SystemInstance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class CheckIncorrectPropertyNames extends ValidationBase{
    
   Map incorrectAndCorrectPropNames = new HashMap<String,String>();

   {
       //incorrect property key : correct property key
       incorrectAndCorrectPropNames.put("java.persistence.provider","javax.persistence.provider");
       incorrectAndCorrectPropNames.put("java.persistence.transactionType","javax.persistence.transactionType");
       incorrectAndCorrectPropNames.put("java.persistence.jtaDataSource","javax.persistence.jtaDataSource");
       incorrectAndCorrectPropNames.put("java.persistence.nonJtaDataSource","javax.persistence.nonJtaDataSource");
       incorrectAndCorrectPropNames.put("java.net.ssl.keyStore","javax.net.ssl.keyStore");
       incorrectAndCorrectPropNames.put("java.net.ssl.keyStorePassword","javax.net.ssl.keyStorePassword");
       incorrectAndCorrectPropNames.put("java.net.ssl.trustStore","javax.net.ssl.trustStore");
       incorrectAndCorrectPropNames.put("java.security.jacc.PolicyConfigurationFactory.provider","javax.security.jacc.PolicyConfigurationFactory.provider");
       incorrectAndCorrectPropNames.put("java.security.jacc.policy.provider","javax.security.jacc.policy.provider");
       incorrectAndCorrectPropNames.put("java.xml.ws.spi.Provider","javax.xml.ws.spi.Provider");
       incorrectAndCorrectPropNames.put("java.xml.soap.MessageFactory","javax.xml.soap.MessageFactory");
       incorrectAndCorrectPropNames.put("java.xml.soap.SOAPFactory","javax.xml.soap.SOAPFactory");
       incorrectAndCorrectPropNames.put("java.xml.soap.SOAPConnectionFactory","javax.xml.soap.SOAPConnectionFactory");
       incorrectAndCorrectPropNames.put("java.xml.soap.MetaFactory","javax.xml.soap.MetaFactory");
       incorrectAndCorrectPropNames.put("java.persistence.sharedCache.mode","javax.persistence.sharedCache.mode");
       incorrectAndCorrectPropNames.put("java.persistence.validation.mode","javax.persistence.validation.mode");
       incorrectAndCorrectPropNames.put("java.persistence.transactionType","javax.persistence.transactionType");

       incorrectAndCorrectPropNames.put("javax.naming.applet","java.naming.applet");
       incorrectAndCorrectPropNames.put("javax.naming.authoritative","java.naming.authoritative");
       incorrectAndCorrectPropNames.put("javax.naming.batchsize","java.naming.batchsize");
       incorrectAndCorrectPropNames.put("javax.naming.dns.url","java.naming.dns.url");
       incorrectAndCorrectPropNames.put("javax.naming.factory.initial","java.naming.factory.initial");
       incorrectAndCorrectPropNames.put("javax.naming.factory.object","java.naming.factory.object");
       incorrectAndCorrectPropNames.put("javax.naming.factory.state","java.naming.factory.state");
       incorrectAndCorrectPropNames.put("javax.naming.factory.url.pkgs","java.naming.factory.url.pkgs");
       incorrectAndCorrectPropNames.put("javax.naming.language","java.naming.language");
       incorrectAndCorrectPropNames.put("javax.naming.provider.url","java.naming.provider.url");
       incorrectAndCorrectPropNames.put("javax.naming.referral","java.naming.referral");
       incorrectAndCorrectPropNames.put("javax.naming.security.authentication","java.naming.security.authentication");
       incorrectAndCorrectPropNames.put("javax.naming.security.credentials","java.naming.security.credentials");
       incorrectAndCorrectPropNames.put("javax.naming.security.principal","java.naming.security.principal");
       incorrectAndCorrectPropNames.put("javax.naming.security.protocol","java.naming.security.protocol");

   }
    
   @Override
   public void validate(AppModule appModule)
   {
       this.module=appModule;
       Properties systemProperties=SystemInstance.get().getProperties();

       Iterator iterator = incorrectAndCorrectPropNames.entrySet().iterator();
       while(iterator.hasNext())
       {
           Map.Entry<String,String> entry = (Map.Entry<String,String>)iterator.next();
           if(systemProperties.containsKey(entry.getKey()))
           {
               warn(appModule.toString(),"incorrect.property.name",entry.getKey(),entry.getValue());
           }  
       }
       
   }
}
