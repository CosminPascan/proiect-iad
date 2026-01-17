package config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;

public class JmsConfig {
    public static void configure(CamelContext camelContext) {
        ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(cf));
    }
}
