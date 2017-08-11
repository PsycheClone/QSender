package org.singular;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.FileUtils;

import javax.jms.*;
import java.io.File;

public class QRetriever {

    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://esb-b-test.erfurt.elex.be:61601");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("com.melexis.ape.rasco.multitest.event");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);

                File file = new File(System.getProperty("user.home") + "/Documents/multitestEvents.txt");
                // Wait for a message
                Message message = consumer.receive(1000);
                do {

                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        System.out.println("Received: " + text);
                        FileUtils.writeStringToFile(file, text + System.lineSeparator(), true);
                    } else {
                        System.out.println("Received message: " + message);
                    }

                    message = consumer.receive(1000);
                } while(message != null);

                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public void onException(JMSException e) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }

    public static void main(String[] args) {
        new Thread(new HelloWorldConsumer()).run();
    }
}
