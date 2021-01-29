import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;

public class TestServer {

    @Test
    public void testGetPort(){
        Assertions.assertTrue(Server.getPort("ServerTmp\\settings.txt") != 0);
    }

    @Test
    public void testLog(){
        try {
            Server.logger("Test");
            String mess = readFile("ServerTmp\\chatLog.txt");

            Assertions.assertEquals(mess, "Test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void testChatHistory(){
        try {
            Server.saveMessage("Test");
            Assertions.assertEquals(Server.getChatHistory(), "Test");
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

    @Test
    public void testConnectingToServer(){
        Server server = new Server();
        server.start();
        new MockClient().start();
        server.interrupt();
    }
}
