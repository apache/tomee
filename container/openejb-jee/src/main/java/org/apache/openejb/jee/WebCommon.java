/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public interface WebCommon extends JndiConsumer {
    String getJndiConsumerName();

    String getContextRoot();

    void setContextRoot(String contextRoot);

    @XmlElement(name = "description", required = true)
    Text[] getDescriptions();

    void setDescriptions(Text[] text);

    String getDescription();

    @XmlElement(name = "display-name", required = true)
    Text[] getDisplayNames();

    void setDisplayNames(Text[] text);

    String getDisplayName();

    Collection<Icon> getIcons();

    Map<String,Icon> getIconMap();

    Icon getIcon();

    List<Empty> getDistributable();

    List<ParamValue> getContextParam();

    List<Filter> getFilter();

    List<FilterMapping> getFilterMapping();

    List<Listener> getListener();

    List<Servlet> getServlet();

    List<ServletMapping> getServletMapping();

    List<SessionConfig> getSessionConfig();

    List<MimeMapping> getMimeMapping();

    List<WelcomeFileList> getWelcomeFileList();

    List<ErrorPage> getErrorPage();

    List<JspConfig> getJspConfig();

    List<SecurityConstraint> getSecurityConstraint();

    List<LoginConfig> getLoginConfig();

    List<SecurityRole> getSecurityRole();

    List<LocaleEncodingMappingList> getLocaleEncodingMappingList();

    List<LifecycleCallback> getPostConstruct();

    List<LifecycleCallback> getPreDestroy();

    List<MessageDestination> getMessageDestination();

    String getId();

    void setId(String value);

    Boolean isMetadataComplete();

    void setMetadataComplete(Boolean value);

    String getVersion();

    void setVersion(String value);
}
