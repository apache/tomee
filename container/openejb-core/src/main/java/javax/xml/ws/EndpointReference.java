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
package javax.xml.ws;

/**
 * This class is only provided so JaxWS code can be compiled under Java6 update 4 which
 * uses JaxWS 2.1.  All of the methods throw UnsupportedOperationException.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class EndpointReference {
    public static EndpointReference readFrom(javax.xml.transform.Source eprInfoset) {
        throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
    }

    public abstract void writeTo(javax.xml.transform.Result result);

    public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features) {
        throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
    }
}
