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

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.JaxbAdapter;
import jakarta.ws.rs.core.Link.JaxbLink;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class JaxbAdapterEx extends XmlAdapter<JaxbLinkEx, Link> {

    /**
     * Convert a {@link JaxbLink} into a {@link Link}.
     * 
     * @param v instance of type {@link JaxbLink}.
     * @return mapped instance of type {@link JaxbLink}
     */
    @Override
    public Link unmarshal(JaxbLinkEx ex) {
        JaxbLink link = new JaxbLink(ex.getUri(), ex.getParams());
        return new JaxbAdapter().unmarshal(link);
    }

    /**
     * Convert a {@link Link} into a {@link JaxbLink}.
     * 
     * @param v instance of type {@link Link}.
     * @return mapped instance of type {@link JaxbLink}.
     */
    @Override
    public JaxbLinkEx marshal(Link v) {
        JaxbLink link = new JaxbAdapter().marshal(v);
        JaxbLinkEx jle = new JaxbLinkEx(link.getUri(), link.getParams());
        return jle;
    }
}
