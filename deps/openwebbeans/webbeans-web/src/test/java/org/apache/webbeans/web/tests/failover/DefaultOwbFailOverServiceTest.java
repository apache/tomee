/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.web.tests.failover;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;

import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.web.failover.DefaultOwbFailOverService;
import org.apache.webbeans.web.failover.FailOverBag;
import org.apache.webbeans.web.failover.FailOverSessionActivationListener;
import org.apache.webbeans.web.tests.MockHttpSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultOwbFailOverServiceTest extends AbstractUnitTest
{
    private static final String SESSION_SCOPED_SAMPLE_TEXT = "session_sample";
    private static final String CONVERSATION_SCOPED_SAMPLE_TEXT = "conversatzion_sample";

    @Before
    public void before()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(ConversationScopedBean.class);
        classes.add(SessionScopedBean.class);

        startContainer(classes);
    }

    @After
    public void after()
    {
        shutDownContainer();
    }

    /**
     * Tests that the {@link FailOverBag} and {@link FailOverSessionActivationListener} will be added to the session.
     */
    @Test
    public void sessionIsIdle()
    {
        // ----- setup
        HttpSession session = new MockHttpSession();

        DefaultOwbFailOverService failoverService =
                (DefaultOwbFailOverService) getWebBeansContext().getService(FailOverService.class);

        failoverService.enableFailOverSupport(true);
        failoverService.enablePassivationSupport(true);



        // ----- execute
        failoverService.sessionIsIdle(session);



        // ----- assert
        FailOverSessionActivationListener listener = (FailOverSessionActivationListener)
                session.getAttribute(FailOverSessionActivationListener.SESSION_ATTRIBUTE_NAME);
        FailOverBag failOverBag = (FailOverBag)
                session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);

        Assert.assertNotNull(listener);
        Assert.assertNotNull(failOverBag);
        Assert.assertFalse(failOverBag.isSessionInUse());
        Assert.assertFalse(failoverService.isPassivation());
    }

    /**
     * Tests that <code>sessionInUse</code> will be set and that the
     * {@link FailOverSessionActivationListener} will be added to the session.
     */
    @Test
    public void sessionIsInUse()
    {
        // ----- setup
        HttpSession session = new MockHttpSession();

        // put bag manually to session
        FailOverBag failOverBag = new FailOverBag(session.getId(), "");
        session.setAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME, failOverBag);

        DefaultOwbFailOverService failoverService =
                (DefaultOwbFailOverService) getWebBeansContext().getService(FailOverService.class);

        failoverService.enableFailOverSupport(true);
        failoverService.enablePassivationSupport(true);



        // ----- execute
        failoverService.sessionIsInUse(session);



        // ----- assert
        FailOverSessionActivationListener listener = (FailOverSessionActivationListener)
                session.getAttribute(FailOverSessionActivationListener.SESSION_ATTRIBUTE_NAME);

        Assert.assertNotNull(listener);
        Assert.assertTrue(failOverBag.isSessionInUse());
        Assert.assertFalse(failoverService.isPassivation());
    }

    /**
     * Tests that the {@link FailOverBag} and {@link FailOverSessionActivationListener} will be added to the session.
     */
    @Test
    public void sessionWillPassivate()
    {
        // ----- setup
        HttpSession session = new MockHttpSession();

        DefaultOwbFailOverService failoverService =
                (DefaultOwbFailOverService) getWebBeansContext().getService(FailOverService.class);

        failoverService.enableFailOverSupport(true);
        failoverService.enablePassivationSupport(true);



        // ----- execute
        failoverService.sessionWillPassivate(session);



        // ----- assert
        FailOverSessionActivationListener listener = (FailOverSessionActivationListener)
                session.getAttribute(FailOverSessionActivationListener.SESSION_ATTRIBUTE_NAME);
        FailOverBag failOverBag = (FailOverBag)
                session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);

        Assert.assertNotNull(listener);
        Assert.assertNotNull(failOverBag);
        Assert.assertFalse(failOverBag.isSessionInUse());
        Assert.assertTrue(failoverService.isPassivation());
    }

    /**
     * Tests that the session context will be stored and restored
     * successfully from the {@link FailOverBag}.
     * 
     * @throws Exception When store/restore the session context fails.
     */
    @Test
    public void restoreSessionContext() throws Exception
    {
        // ----- setup
        HttpSession session = new MockHttpSession();

        DefaultOwbFailOverService failoverService =
                (DefaultOwbFailOverService) getWebBeansContext().getService(FailOverService.class);

        failoverService.enableFailOverSupport(true);
        failoverService.enablePassivationSupport(true);



        // ----- execute
        getWebBeansContext().getContextsService().startContext(SessionScoped.class, session);

        // set sample text
        getInstance(SessionScopedBean.class).setText(SESSION_SCOPED_SAMPLE_TEXT);

        // store beans in session
        failoverService.sessionIsIdle(session);

        // serialize / deserialize and store deserialized bag back to session
        FailOverBag failOverBag;
        failOverBag = (FailOverBag) session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);
        failOverBag = (FailOverBag) serialize(failOverBag);

        session.setAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME, failOverBag);

        // remove sample text
        getInstance(SessionScopedBean.class).setText(null);

        getWebBeansContext().getContextsService().endContext(SessionScoped.class, session);

        // restore beans from session
        failoverService.sessionDidActivate(session);

        getWebBeansContext().getContextsService().startContext(SessionScoped.class, session);



        // ----- assert
        Assert.assertEquals(SESSION_SCOPED_SAMPLE_TEXT, getInstance(SessionScopedBean.class).getText());
    }

    /**
     * Tests that the conversation contexts will be stored and restored
     * successfully from the {@link FailOverBag}.
     * 
     * @throws Exception When store/restore the session context fails.
     */
    @Test
    public void restoreConversationContexts() throws Exception
    {
        // ----- setup
        HttpSession session = new MockHttpSession();

        DefaultOwbFailOverService failoverService =
                (DefaultOwbFailOverService) getWebBeansContext().getService(FailOverService.class);

        failoverService.enableFailOverSupport(true);
        failoverService.enablePassivationSupport(true);



        // ----- execute
        // create conversation
        ConversationContext conversationContext = new ConversationContext();
        conversationContext.setActive(true);

        Conversation conversation = new ConversationImpl(session.getId(), getWebBeansContext());
        conversation.begin();

        ConversationManager conversationManager = getWebBeansContext().getConversationManager();
        conversationManager.addConversationContext(conversation, conversationContext);

        getWebBeansContext().registerService(ConversationService.class, new MockConversationService(session, conversation));

        // set sample text
        getInstance(ConversationScopedBean.class, conversationContext).setText(CONVERSATION_SCOPED_SAMPLE_TEXT);

        // store beans in session
        failoverService.sessionIsIdle(session);

        // serialize / deserialize and store deserialized bag back to session
        FailOverBag failOverBag;
        failOverBag = (FailOverBag) session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);
        failOverBag = (FailOverBag) serialize(failOverBag);

        session.setAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME, failOverBag);

        // remove sample text
        getInstance(ConversationScopedBean.class, conversationContext).setText(null);

        getWebBeansContext().getContextsService().endContext(ConversationScoped.class, conversationContext);

        // restore beans from session
        failoverService.sessionDidActivate(session);



        // ----- assert
        // get and activate conversation
        Map<Conversation, ConversationContext> conversationMap =
                conversationManager.getConversationMapWithSessionId(session.getId());

        Conversation propogatedConversation =
                conversationManager.getPropogatedConversation(conversation.getId(), session.getId());

        ConversationContext propogatedConversationContext =  conversationMap.get(propogatedConversation);
        propogatedConversationContext.setActive(true);

        ConversationScopedBean beanInstance = getInstance(ConversationScopedBean.class, propogatedConversationContext);

        Assert.assertEquals(CONVERSATION_SCOPED_SAMPLE_TEXT, beanInstance.getText());
        Assert.assertTrue(beanInstance.isBeanManagerNotNull());
    }

    /**
     * Tests that the {@link FailOverSessionActivationListener} is serializable.
     *
     * @throws Exception If serialization fails.
     */
    @Test
    public void serializeSessionActivationListener() throws Exception
    {
        serialize(new FailOverSessionActivationListener());
    }

    private Serializable serialize(Serializable serializable) throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(serializable);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        Object deserializedObject = ois.readObject();

        ois.close();

        return (Serializable) deserializedObject;
    }

    public <T> T getInstance(Class<T> type, Context context)
    {
        Set<Bean<?>> beans = getBeanManager().getBeans(type);
        Bean<T> bean = (Bean<T>) getBeanManager().resolve(beans);
        CreationalContext<T> creationalContext = getBeanManager().createCreationalContext(bean);

        return context.get(bean, creationalContext);
    }
}
