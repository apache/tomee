/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.assembler.spring;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.xbean.spring.context.SpringApplicationContext;
import org.openejb.OpenEJBException;

/**
 * @version $Revision$ $Date$
 */
public class AssemblerUtil {
    public static <T> List<T> asList(T[] array) {
        List<T> list;
        if (array != null) {
            list = Arrays.asList(array);
        } else {
            list = Collections.emptyList();
        }
        return list;
    }

    public static void addSystemJndiProperties() {
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String str = systemProperties.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            String naming = "org.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = naming + ":" + str;
            }
            systemProperties.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(SpringApplicationContext factory, Class<T> type) throws OpenEJBException {
        // get the main service from the configuration file
        String[] names = factory.getBeanNamesForType(type);
        if (names.length == 0) {
            throw new OpenEJBException("No bean of type: " + type.getName() + " found in the bootstrap file: " + factory.getDisplayName());
        }
        T bean = (T) factory.getBean(names[0]);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(SpringApplicationContext factory, Class<T> type) {
        // get the main service from the configuration file
        String[] names = factory.getBeanNamesForType(type);
        ArrayList<T> beans = new ArrayList<T>(names.length);
        for (String name : names) {
            beans.add((T) factory.getBean(name));
        }
        return beans;
    }
}
