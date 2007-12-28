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
package org.apache.openejb.tomcat;

import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.openejb.tomcat.naming.LegacyAnnotationProcessor;

/**
 * Processes annotations for a v5 Tomcat install
 * Listener is added to each StandardWrapper
 *
 * @version $Rev$ $Date$
 */
public class ProcessAnnotatedServletsListener extends LegacyAnnotationProcessorListener implements InstanceListener {

    public ProcessAnnotatedServletsListener(LegacyAnnotationProcessor annotationProcessor) {
        super(annotationProcessor);
    }

    public void instanceEvent(InstanceEvent event) {
        String type = event.getType();
        if (InstanceEvent.BEFORE_INIT_EVENT.equals(type)) {
            beforeInit(event);
        } else if (InstanceEvent.AFTER_DESTROY_EVENT.equals(type)) {
            afterDestroy(event);
        }
    }

    private void beforeInit(InstanceEvent event) {
        Object object = event.getServlet();
        processAnnotations(object);
        postConstruct(object);
    }

    private void afterDestroy(InstanceEvent event) {
        Object object = event.getServlet();
        preDestroy(object);
    }

}
