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
package org.apache.openejb.arquillian.tests.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.ejbjar31.EjbJarDescriptor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

@Ignore
@RunWith(Arquillian.class)
public class EnvEntryTest {
    @EJB
    private MessageBean messageBean;

    @Deployment
    public static JavaArchive getArchive() {

        final EjbJarDescriptor ejbJarDescriptor = Descriptors.create(EjbJarDescriptor.class);
        ejbJarDescriptor.getOrCreateEnterpriseBeans()
            .createMessageDriven()
                .ejbName("RedBean")
                .ejbClass(RedBean.class.getName())
                .messagingType("jakarta.jms.MessageListener")
                .transactionType("Container")
                .messageDestinationType("jakarta.jms.Topic")
                .getOrCreateActivationConfig()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destinationType")
                        .activationConfigPropertyValue("jakarta.jms.Topic").up()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destination")
                        .activationConfigPropertyValue("red").up().up()
                .createEnvEntry().envEntryName("color").envEntryType("java.lang.String").envEntryValue("red").up().up()
            .createMessageDriven()
                .ejbName("BlueBean")
                .ejbClass(BlueBean.class.getName())
                .messagingType("jakarta.jms.MessageListener")
                .transactionType("Container")
                .messageDestinationType("jakarta.jms.Topic")
                .getOrCreateActivationConfig()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destinationType")
                        .activationConfigPropertyValue("jakarta.jms.Topic").up()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destination")
                        .activationConfigPropertyValue("blue").up().up()
                .createEnvEntry().envEntryName("color").envEntryType("java.lang.String").envEntryValue("blue").up().up()
            .createMessageDriven()
                .ejbName("NoColorBean")
                .ejbClass(NoColorSpecifiedBean.class.getName())
                .messagingType("jakarta.jms.MessageListener")
                .transactionType("Container")
                .messageDestinationType("jakarta.jms.Topic")
                .getOrCreateActivationConfig()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destinationType")
                        .activationConfigPropertyValue("jakarta.jms.Topic").up()
                    .createActivationConfigProperty()
                        .activationConfigPropertyName("destination")
                        .activationConfigPropertyValue("nocolor").up().up();

        final String ejbJarXml = ejbJarDescriptor.exportAsString();


        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jms-env-entry.jar")
                .addClasses(BaseMdbBean.class, BlueBean.class, Color.class, NoColorSpecifiedBean.class, RedBean.class, MessageBean.class)
                .add(new StringAsset("<beans/>"), "META-INF/beans.xml")
                .add(new StringAsset(ejbJarXml), "META-INF/ejb-jar.xml");

        System.out.println(archive.toString(true));

        return archive;
    }

    @Test
    public void test() throws Exception {
        messageBean.clear();

        messageBean.callRed();
        assertXMessages(1);
        assertEquals("red",messageBean.getColors().get(0));
        messageBean.clear();

        messageBean.callBlue();
        assertXMessages(1);
        assertEquals("blue",messageBean.getColors().get(0));
        messageBean.clear();

        messageBean.callNoColor();
        assertXMessages(1);
        assertEquals("<not specified>",messageBean.getColors().get(0));
        messageBean.clear();
    }

    public void assertXMessages(final int x) {
        attempt(5, 100, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                assertEquals(x, messageBean.getColors().size());
                return null;
            }
        });
    }


    public <T> T attempt(int tries, int delay, Callable<T> callable) {
        for (int i = 0; i < tries; i++) {
            try {
                return callable.call();
            } catch (Throwable t) {
                if (i == (tries - 1)) {
                    throw new RuntimeException(t);
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        throw new IllegalStateException("We shouldn't reach this exception");
    }
}
