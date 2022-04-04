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


package org.apache.tomee.itests.ejb;

import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PortReuseTest {

    @Test
    public void test() throws Exception {
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(CalculatorRemote.class)
                        .add(CalculatorBean.class)
                        .asJar())
                .home(this::editAccessLogPattern)
                .home(this::updateSystemProperties)
                .build();

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        p.setProperty(Context.PROVIDER_URL, "http://localhost:" + tomee.getPort() + "/tomee/ejb");

        final InitialContext initialContext = new InitialContext(p);
        final CalculatorRemote calc = (CalculatorRemote) initialContext.lookup("CalculatorBeanRemote");
        Assert.assertEquals(3, calc.sum(1, 2));
        Assert.assertEquals(6, calc.sum(2, 4));
        Assert.assertEquals(12, calc.sum(3, 9));
        Assert.assertEquals(24, calc.sum(4, 20));

        final List<String> ejbRequests = getEjbRequests(5, tomee.getHome(), 30);

        final Set<String> sourcePorts = ejbRequests.stream()
                .map(this::lastNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        System.out.println(Join.join(", ", sourcePorts));
        Assert.assertEquals(1, sourcePorts.size());
        Assert.assertEquals(5, ejbRequests.size());
    }

    private List<String> getEjbRequests(final int expected, final File tomeeHome, final int retries) {
        // The access log is written to asynchronously, so we poll it up to <retries> times.
        for (int i = 0; i < retries; i++) {
            final List<String> ejbRequests = getEjbRequests(tomeeHome);

            if (ejbRequests != null && ejbRequests.size() >= expected) {
                return ejbRequests;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        return null;
    }

    private List<String> getEjbRequests(final File tomeeHome) {
        try {
            final File accessLog = findAccessLog(tomeeHome);
            final String requests = IO.slurp(accessLog);

            final List<String> ejbRequests = Arrays.stream(requests.split("([\\r\\n])+"))
                    .filter(r -> r.contains("/tomee/ejb"))
                    .collect(Collectors.toList());

            return ejbRequests;
        } catch (Throwable t) {
            // ignore
        }

        return null;
    }

    private String lastNumber(final String input) {
        final Pattern lastNumber = Pattern.compile("^.*?(\\d+)$");
        final Matcher matcher = lastNumber.matcher(input);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private File findAccessLog(final File home) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final File logsDirectory = new File(home, "logs");
        final String expectedFilename = "localhost_access_log." + sdf.format(new Date()) + ".txt";

        final File[] accessLogs = logsDirectory.listFiles(f -> f.getName().equals(expectedFilename));

        if (accessLogs.length != 1) {
            throw new RuntimeException("Unable to find access log");
        }

        return accessLogs[0];
    }

    private void updateSystemProperties(File home) {
        try {
            final File systemProps = Files.file(home, "conf", "system.properties");
            String props = IO.slurp(systemProps);
            props = props + "\ntomee.remote.support = true" +
                    "\ntomee.serialization.class.whitelist = java.,org.apache.openejb.,org.apache.tomee." +
                    "\ntomee.serialization.class.blacklist = -";

            IO.copy(IO.read(props), systemProps);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void editAccessLogPattern(final File home) {
        try {
            final File serverxml = Files.file(home, "conf", "server.xml");
            final String config = IO.slurp(serverxml)
                    .replaceAll("pattern=\".*?\"", "pattern=\"%h %l %u %t &quot;%r&quot; %s %b %{remote}p\"");

            IO.copy(IO.read(config), serverxml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}