package org.singular;

import org.apache.commons.io.FileUtils;

import javax.jms.TextMessage;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class SocketSender {

    public static class SocketSenderProducer implements Runnable {
        public void run() {
            try {
                final String host = "localhost";
                final int portNumber = 22300;
                System.out.println("Creating socket to '" + host + "' on port " + portNumber);

                File file = new File(getClass().getClassLoader().getResource("toRemove/mt01.txt").getFile());
                List<String> lines = FileUtils.readLines(file);
                for (String line : lines) {
                    Thread.sleep(50);
                    Socket socket = new Socket(host, portNumber);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    out.println(line);
                    System.out.println("Sent line " + line);

                    socket.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        new Thread(new SocketSenderProducer()).run();
    }
}
