package testServer;
import DirServer.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TestServer {
    Server server = new Server();

    @Test
    public void testMessageFormatAndCommandExit(){
        try {
            server.start();
            server.command("/logOF");
            server.command("/clearChat");
            MockClient mock = new MockClient();
            mock.start();

            while(!server.getChatHistory().contains("TestTime"));
            Assertions.assertTrue(server.getChatHistory().contains(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
            Assertions.assertTrue(server.getChatHistory().contains("NoName"));

            server.command("/exit");
            Thread.sleep(1000);
            Assertions.assertFalse(server.isAlive());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetPort(){
            Assertions.assertTrue(Server.getPort("ServerTmp/settings.txt") == 8955);
    }

    @Test
    public void testLog(){
        try {
            String logBuff = readFile("ServerTmp/chatLog.txt");
            writeFile("ServerTmp/chatLog.txt", "");
            Server.command("/logON");

            Server.logger("Test");
            System.out.println(readFile("ServerTmp/chatLog.txt"));
            Assertions.assertTrue(readFile("ServerTmp/chatLog.txt").contains("Test"));

            Server.logger("TestLog");
            Assertions.assertTrue(readFile("ServerTmp/chatLog.txt").contains("Test"));

            writeFile("ServerTmp/chatLog.txt", logBuff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void testChatHistory(){
        try {
            String test = "Test";
            Server.saveMessage(test);
            Assertions.assertTrue(Server.getChatHistory().contains(test));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        int i;
        String mess = "";
        FileReader reader = new FileReader(filePath);
        while((i = reader.read()) != - 1){
            mess += String.valueOf((char)i);
        }
        return mess;
    }

    private static void writeFile(String filePath, String writeText) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(writeText);
        writer.flush();
    }

}
