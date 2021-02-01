package DirClient;

import java.io.*;
import java.net.Socket;

public class ThreadMessagesListener extends Thread {

    private Socket clientSocket;

    public ThreadMessagesListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Единственная роль потока - слушать и выводить сообщения в консоль
     */
    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            while (!clientSocket.isClosed()) {
                String mess = in.readUTF();
                Client.logger(mess);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Client.logger("Поток чтения сообщений завершил работу!");
            } catch (IOException ignore) {}
        }
    }
}
