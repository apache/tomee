/*
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
package org.apache.tomee.catalina.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.IOException;
import java.io.Writer;

// this valve simply print the http error code
// it is to avoid to get any information on the server
// no tomcat/tomee version, no css, no stacktrace should be printed
public class MinimumErrorReportValve extends ErrorReportValve {
    @Override
    protected void report(final Request request, final Response response,
                          final Throwable throwable) {
        final int statusCode = response.getStatus();
        if (statusCode < 400 || response.getContentWritten() > 0 ||
                !response.isError()) {
            return;
        }

        try {
            try {
                response.setContentType("text/html");
                response.setCharacterEncoding("utf-8");
            } catch (final Throwable t) {
                ExceptionUtils.handleThrowable(t);
                if (container.getLogger().isDebugEnabled()) {
                    container.getLogger().debug("status.setContentType", t);
                }
            }
            final Writer writer = response.getReporter();
            if (writer != null) {
                writer.write("<html>\n" +
                        "<head>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<h1>HTTP Error " + statusCode +"</h1>\n" +
                        "</body>\n" +
                        "</html>\n");
            }
        } catch (final IOException | IllegalStateException e) {
            // Ignore
        }
    }
}
