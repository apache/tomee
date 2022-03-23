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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.webservices.saaj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SAAJMetaFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;

public class SaajMetaFactoryImpl extends SAAJMetaFactory {

    @Override
    protected MessageFactory newMessageFactory(String arg0) throws SOAPException {
        return (MessageFactory) callFactoryMethod("newMessageFactory", arg0);
    }

    @Override
    protected SOAPFactory newSOAPFactory(String arg0) throws SOAPException {
        return (SOAPFactory) callFactoryMethod("newSOAPFactory", arg0);
    }

    private Object callFactoryMethod(String methodName, String arg) throws SOAPException {
        SAAJMetaFactory factory =
            (SAAJMetaFactory) SaajFactoryFinder.find("jakarta.xml.soap.MetaFactory");

        try {
            Method method =
                factory.getClass().getDeclaredMethod(methodName, new Class[]{String.class});
            boolean accessibility = method.isAccessible();
            try {
                method.setAccessible(true);
                Object result = method.invoke(factory, new Object[]{arg});
                return result;
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof SOAPException) {
                    throw (SOAPException) e.getTargetException();
                } else {
                    throw new SOAPException("Error calling factory method: " + methodName, e);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new SOAPException("Error calling factory method: " + methodName, e);
            } finally {
                method.setAccessible(accessibility);
            }
        } catch (NoSuchMethodException e) {
            throw new SOAPException("Factory method not found: " + methodName, e);
        }
    }

}
