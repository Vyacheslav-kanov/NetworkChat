package DirServer;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerService implements Runnable {

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;

    private String clientName;

    public ServerService(Socket client) {
        try {
            this.socket = client;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            clientName = getClientName();//Записываем имя пользователя.
            sendingHistoricalMessages();//Отправляем пользователю в ответ историю переписки.

            /**
             * После того как получили имя пользователя, попреветствуем его,
             * начнем читать его сообщения, проверять на команду "/exit",
             * рассылать остальным пользователям уведомление о новом пользователе.
             * В конце выполнения потока разошлем полседнее сообщение,
             * что пользователь покинул чат, закроем потоки и сокет.
             */
            send(clientName + " присоединился к чату!");

            String mess;
            while (!socket.isClosed()) {
                mess = messageFormat(in.readUTF(), clientName);
                System.out.println(mess);
                send(mess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                send(clientName + " покинул чат!");
                Server.logger(clientName + " покинул чат!");

                in.close();
                out.flush();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод предназначен для рассылки сообщений всем кто прнисоединился к серверу
     * и попал в список сокетов.
     * Цикл итерируется по списку и отправляет каждому сокету сообщение.
     * Если сокет закрыт пропускаем, если это наш сокет пропускаем.
     * @param message
     * @throws IOException
     */
    private void send(String message) throws IOException {
        for (Socket e : Server.getServerList()) {
            if (e.isClosed()) continue;
            if (e.equals(socket)) continue;
            DataOutputStream sender = new DataOutputStream(e.getOutputStream());
            sender.writeUTF(message);
            sender.flush();
        }
        Server.logger(message);
        Server.saveMessage(message);
    }

    /**
     * Метод форматирует текст для большей информативности и аккуратности в чате.
     * @param message
     * @param clientName
     * @return
     */
    private static String messageFormat(String message, String clientName){
        return "(" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ") " + clientName + ": " + message;
    }

    /**
     * Принимает от пользователя имя, отправляет в ответ
     * сообщение "Добро пожаловать!" и возвращает имя пользователя.
     * также отправляет отчет в консоль и лог
     * @return
     * @throws IOException
     */
    private String getClientName() throws IOException {
        String report = "Жду имени пользователя";
        Server.logger(report);
        final String clientName = in.readUTF();
        out.writeUTF(clientName + " Добро пожаловать!");
        out.flush();
        return clientName;
    }

    /**
     * Метод отправляет клиенту историю сообщений.
     * Также отправляет отчет в лог.
     * @throws IOException
     */
    private void sendingHistoricalMessages() throws IOException {
        Server.logger("Отправка истории сообщений...");
        out.writeUTF(Server.getChatHistory());
        out.flush();
    }
}
