package org.singular;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sven on 11/10/2017.
 */
public class TestSocket {
    public static void main(String[] args) throws IOException {
        LocalDateTime from = LocalDateTime.parse("2017-10-06T07:00:00");
        LocalDateTime till = LocalDateTime.parse("2017-10-12T10:00:00");

        while (from.isBefore(till)) {
            LocalDateTime toNextReport = from.plusMinutes(30);
            getJams(from, toNextReport);
            from = toNextReport;
        }
    }

    private static void getJams(LocalDateTime from, LocalDateTime till) {
        Map<String, Integer> jamsTotal = new HashMap<>();
        String fromDate = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String tillDate = till.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try(
                Socket socket = new Socket("pc3186.erfurt.elex.be", 22000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String xml = "<?xml version=\"1.0\" encoding=\"iso8859-1\"?>\n" +
                    "<OEE-Analyzer File-Type=\"Command\" File-Version=\"V1.00\">\n" +
                    "<Command Name=\"HistorySingleFailureEquipment\" EquipmentProductionID=\"MT 12\" Start=\"" + fromDate + "\" End=\"" + tillDate + "\" Resolution=\"hourly\" Summary=\"Yes\" />\n" +
                    "</OEE-Analyzer>";
            out.println(xml);
            String response = in.readLine();
//            System.out.println(response);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));

            NodeList jamNodes = doc.getFirstChild().getFirstChild().getFirstChild().getChildNodes();
            if(jamNodes.getLength() > 0) {
                for(int i = 0; i < jamNodes.getLength(); i++) {
                    NamedNodeMap jamAttributes = jamNodes.item(i).getAttributes();
                    String jam = jamAttributes.getNamedItem("SubModule").getNodeValue();
                    String total = jamAttributes.getNamedItem("Total").getNodeValue();
                    if(jamsTotal.containsKey(jam)) {
                        int newTotal = jamsTotal.get(jam);
                        newTotal += Integer.parseInt(total);
                        jamsTotal.put(jam, newTotal);
                    } else {
                        jamsTotal.put(jam, Integer.parseInt(total));
                    }
                }
            }
        }  catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        System.out.println(String.format("%s - %s: %s", fromDate, tillDate, jamsTotal));
    }
}
