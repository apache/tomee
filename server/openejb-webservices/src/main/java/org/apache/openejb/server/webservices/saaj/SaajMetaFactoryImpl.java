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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SAAJMetaFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

public class SaajMetaFactoryImpl extends SAAJMetaFactory {

    protected MessageFactory newMessageFactory(final String arg0) throws SOAPException {
        return (MessageFactory) callFactoryMethod("newMessageFactory", arg0);
    }

    protected SOAPFactory newSOAPFactory(final String arg0) throws SOAPException {
        return (SOAPFactory) callFactoryMethod("newSOAPFactory", arg0);
    }

    private Object callFactoryMethod(final String methodName, final String arg) throws SOAPException {
        final SAAJMetaFactory factory =
            (SAAJMetaFactory) SaajFactoryFinder.find("javax.xml.soap.MetaFactory");

        try {
            final Method method =
                factory.getClass().getDeclaredMethod(methodName, new Class[]{String.class});
            final boolean accessibility = method.isAccessible();
            try {
                method.setAccessible(true);
                final Object result = method.invoke(factory, new Object[]{arg});
                return result;
            } catch (final InvocationTargetException e) {
                if (e.getTargetException() instanceof SOAPException) {
                    throw (SOAPException) e.getTargetException();
                } else {
                    throw new SOAPException("Error calling factory method: " + methodName, e);
                }
            } catch (final IllegalArgumentException e) {
                throw new SOAPException("Error calling factory method: " + methodName, e);
            } catch (final IllegalAccessException e) {
                throw new SOAPException("Error calling factory method: " + methodName, e);
            } finally {
                method.setAccessible(accessibility);
            }
        } catch (final NoSuchMethodException e) {
            throw new SOAPException("Factory method not found: " + methodName, e);
        }
    }

}
