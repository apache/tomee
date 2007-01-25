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
package org.apache.openejb.config;

public interface Service {
    public java.lang.String getContent();

    public java.lang.String getId();

    public java.lang.String getJar();

    public java.lang.String getProvider();

    public void setContent(java.lang.String content);

    public void setId(java.lang.String id);

    public void setJar(java.lang.String jar);

    public void setProvider(java.lang.String provider);

    public void validate() throws org.exolab.castor.xml.ValidationException;
}