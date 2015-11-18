/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.apache.openejb.resource.activemq;

import javax.jms.*;
import java.io.Serializable;

public class SessionWrapper implements Session {

    private final ConnectionWrapper connectionWrapper;
    private final Session session;

    public SessionWrapper(final ConnectionWrapper connectionWrapper, final Session session) {
        this.connectionWrapper = connectionWrapper;
        this.session = session;
    }

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        return session.createBytesMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return session.createTextMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        return session.createMessage();
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return session.getTransacted();
    }

    @Override
    public void rollback() throws JMSException {
        session.rollback();
    }

    @Override
    public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean NoLocal) throws JMSException {
        return session.createConsumer(destination, messageSelector, NoLocal);
    }

    @Override
    public QueueBrowser createBrowser(final Queue queue) throws JMSException {
        return session.createBrowser(queue);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return session.createTemporaryQueue();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return session.createMapMessage();
    }

    @Override
    public MessageConsumer createConsumer(final Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }

    @Override
    public void close() throws JMSException {
        try {
            session.close();
        } finally {
            this.connectionWrapper.remove(this);
        }
    }

    @Override
    public void unsubscribe(final String name) throws JMSException {
        session.unsubscribe(name);
    }

    @Override
    public ObjectMessage createObjectMessage(final Serializable object) throws JMSException {
        return session.createObjectMessage(object);
    }

    @Override
    public void run() {
        session.run();
    }

    @Override
    public void recover() throws JMSException {
        session.recover();
    }

    @Override
    public void commit() throws JMSException {
        session.commit();
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return session.getAcknowledgeMode();
    }

    @Override
    public TextMessage createTextMessage(final String text) throws JMSException {
        return session.createTextMessage(text);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException {
        return session.createDurableSubscriber(topic, name, messageSelector, noLocal);
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return session.createObjectMessage();
    }

    @Override
    public Topic createTopic(final String topicName) throws JMSException {
        return session.createTopic(topicName);
    }

    @Override
    public void setMessageListener(final MessageListener listener) throws JMSException {
        session.setMessageListener(listener);
    }

    @Override
    public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException {
        return session.createBrowser(queue, messageSelector);
    }

    @Override
    public MessageProducer createProducer(final Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    @Override
    public Queue createQueue(final String queueName) throws JMSException {
        return session.createQueue(queueName);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException {
        return session.createDurableSubscriber(topic, name);
    }

    @Override
    public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException {
        return session.createConsumer(destination, messageSelector);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return session.createStreamMessage();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return session.getMessageListener();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return session.createTemporaryTopic();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SessionWrapper that = (SessionWrapper) o;

        return connectionWrapper.equals(that.connectionWrapper) && session.equals(that.session);

    }

    @Override
    public int hashCode() {
        int result = connectionWrapper.hashCode();
        result = 31 * result + session.hashCode();
        return result;
    }
}
