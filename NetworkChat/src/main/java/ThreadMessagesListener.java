import java.io.*;
import java.net.Socket;

public class ThreadMessagesListener extends Thread {

    private Socket clientSocket;

    public ThreadMessagesListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            while (!clientSocket.isClosed()) {
                String mess = in.readUTF();
                System.out.println(mess);
                Client.loger(mess);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Поток чтения сообщений завершил работу!");
        }
    }
}
