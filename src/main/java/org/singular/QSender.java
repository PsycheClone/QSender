package org.singular;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.FileUtils;

import javax.jms.*;
import java.io.File;
import java.util.List;

public class QSender {
    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("com.melexis.ape.rasco.multitest.event");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                File file = new File(getClass().getClassLoader().getResource("multitestEvents.txt").getFile());
                List<String> lines = FileUtils.readLines(file);
                for(String line : lines) {
                    TextMessage message = session.createTextMessage(line);
                    producer.send(message);
                    System.out.println("Sent " + line);
//                    Thread.sleep(500);
                }

                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new HelloWorldProducer()).run();
    }
}
