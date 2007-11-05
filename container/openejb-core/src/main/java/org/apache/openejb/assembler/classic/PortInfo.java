/**
 *
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

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

public class PortInfo extends InfoObject {
    public String portId;
    public String serviceName;
    public String portName;
    public String seiInterfaceName;
    public String wsdlFile;
    public String serviceLink;
    public final List<HandlerChainInfo> handlerChains = new ArrayList<HandlerChainInfo>();
    public boolean mtomEnabled;
    public String binding;
    public QName wsdlPort;
    public QName wsdlService;
    public String location;
}
