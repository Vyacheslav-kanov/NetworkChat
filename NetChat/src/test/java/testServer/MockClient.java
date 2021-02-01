package testServer;

import DirServer.Server;
import org.junit.jupiter.api.Assertions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MockClient extends Thread{

    private static int port = Server.getPort("ServerTmp/settings.txt");

    @Override
    public void run(){
        try(Socket socket = new Socket("localhost", port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream((socket.getInputStream()))
        ){
            out.writeUTF("NoName");
            String response;
            while((response = in.readUTF()).isEmpty());
            response = null;

            while((response = in.readUTF()).isEmpty());
            out.writeUTF("TestTime");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
