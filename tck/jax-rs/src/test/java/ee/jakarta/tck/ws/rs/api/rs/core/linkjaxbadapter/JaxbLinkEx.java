/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.api.rs.core.linkjaxbadapter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * JaxbLink with setters and xml annotations
 */
@XmlRootElement
public class JaxbLinkEx {

    protected URI uri;

    protected Map<QName, Object> params;

    /**
     * Default constructor needed during unmarshalling.
     */
    public JaxbLinkEx() {
    }

    /**
     * Construct an instance from a URI and no parameters.
     * 
     * @param uri underlying URI.
     */
    public JaxbLinkEx(URI uri) {
        this.uri = uri;
    }

    /**
     * Construct an instance from a URI and some parameters.
     * 
     * @param uri    underlying URI.
     * @param params parameters of this link.
     */
    public JaxbLinkEx(URI uri, Map<QName, Object> params) {
        this.uri = uri;
        this.params = params;
    }

    /**
     * Get the underlying URI for this link.
     * 
     * @return underlying URI.
     */
    @XmlAttribute(name = "href")
    public URI getUri() {
        return uri;
    }

    /**
     * Get the parameter map for this link.
     * 
     * @return parameter map.
     */
    @XmlAnyAttribute
    public Map<QName, Object> getParams() {
        if (params == null) {
            params = new HashMap<QName, Object>();
        }
        return params;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    public void setParams(final Map<QName, Object> params) {
        this.params = params;
    }
}
