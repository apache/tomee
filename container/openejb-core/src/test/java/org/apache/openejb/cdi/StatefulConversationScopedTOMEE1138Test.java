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
package org.apache.openejb.cdi;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateful;
import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class StatefulConversationScopedTOMEE1138Test {
    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{MyConversation.class};
    }

    @Inject
    private BeanManager bm;

    @Inject
    private MyConversation conversation;

    @Inject
    @Named("myConversation")
    private MyConversation conversationByName;

    @Before
    public void startConversation() {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        webBeansContext.registerService(ConversationService.class, new ConversationService() {
            @Override
            public String getConversationId() {
                return "conversation-test";
            }

            @Override
            public String generateConversationId() {
                return "cid_1";
            }
        });
        webBeansContext.getService(ContextsService.class).startContext(ConversationScoped.class, null);
    }

    @After
    public void stopConversation() {
        WebBeansContext.currentInstance().getService(ContextsService.class).endContext(ConversationScoped.class, null);
    }

    private static void doTest(final MyConversation conversation) {
        assertEquals("John", conversation.getName());
        conversation.setName("foo1");
        assertEquals("foo1", conversation.getName());
    }

    @Test
    public void injectionByType() {
        assertNotNull(conversation);
        doTest(conversation);
    }

    @Test
    public void injectionByName() {
        assertNotNull(conversationByName);
        doTest(conversationByName);
    }

    @Test
    public void injectionByNameLookup() {
        final Bean<?> myConversation = bm.resolve(bm.getBeans("myConversation"));
        final MyConversation conv = MyConversation.class.cast(bm.getReference(myConversation, Object.class, null));
        doTest(conv);
    }

    @Test
    public void el() {
        final BeanELResolver elResolver = new BeanELResolver();
        assertEquals("John", elResolver.getValue(new ELContext() {
            @Override
            public ELResolver getELResolver() {
                return elResolver;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return new FunctionMapper() {
                    @Override
                    public Method resolveFunction(final String prefix, final String localName) {
                        return null;
                    }
                };
            }

            @Override
            public VariableMapper getVariableMapper() {
                return new VariableMapper() {
                    @Override
                    public ValueExpression resolveVariable(final String variable) {
                        return null;
                    }

                    @Override
                    public ValueExpression setVariable(final String variable, final ValueExpression expression) {
                        return null;
                    }
                };
            }
        }, conversationByName, "name"));
    }

    @Test
    public void properties() throws IntrospectionException {
        final BeanInfo info = Introspector.getBeanInfo(conversationByName.getClass());
        final PropertyDescriptor[] pds = info.getPropertyDescriptors();
        assertEquals(2, pds.length); // class and name
    }

    @Named
    @Stateful
    @ConversationScoped
    public static class MyConversation implements Serializable {
        private String name = "John";

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
