package testClient;
import DirClient.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TestClient {

    @Test
    public void testGetPort(){
        Assertions.assertTrue(Client.getPort("ClientTmp/settings.txt") == 8955);
    }

    @Test
    public void testGetIp(){
        Assertions.assertTrue(Client.getIp("ClientTmp/settings.txt").equals("127.0.0.1"));
    }

    @Test
    public void testLogger(){
        try {
            String logBuff = readFile("ClientTmp/chatLog.txt");
            writeFile("ServerTmp/chatLog.txt", "");

            Client.logger("Test");
            Assertions.assertTrue(readFile("ClientTmp/chatLog.txt").contains("Test"));

            Client.logger("TestLog");
            Assertions.assertTrue(readFile("ClientTmp/chatLog.txt").contains("Test"));

            writeFile("ClientTmp/chatLog.txt", logBuff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String readFile(String filePath) throws IOException {
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
