package org.superbiz.application.villain;

import javax.jms.JMSDestinationDefinition;

@JMSDestinationDefinition(
        name= ConfigureJMSDestinations.TOPIC_NAME,
        interfaceName = "javax.jms.Topic"
)
public class ConfigureJMSDestinations {
    public static final String TOPIC_NAME = "java:/jms/topics/villains";
}
