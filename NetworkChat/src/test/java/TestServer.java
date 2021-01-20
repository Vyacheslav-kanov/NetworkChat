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
            Server.loger("Test");
            String mess = readFile("ServerTmp\\chatLog.txt");

            Assertions.assertEquals(mess, "Test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void testChatHistory(){
        Server.saveMessage("Test");
        Assertions.assertEquals(Server.getChatHistory(), "Test");
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
}
