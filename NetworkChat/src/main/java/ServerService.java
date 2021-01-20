import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerService implements Runnable {

    private static DataInputStream in;
    private static DataOutputStream out;

    private Socket socket;

    private String clientName;

    public ServerService(Socket client) {
        this.socket = client;
    }

    @Override
    public void run() {
        try{
            System.out.println(socket);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            clientName = getClientName();//Записываем имя пользователя.
            sendingHistoricalMessages();//Отправляем пользователю в ответ историю переписки.

            /**
             * После того как получили имя пользователя, попреветствуем его,
             * начнем читать его сообщения, проверять на команду "/exit",
             * рассылать остальным пользователям уведомление о новом пользователе.
             * В конце выполнения потока разошлем полседнее сообщение,
             * что пользователь покинул чат, закроем потоки и сокет.
             */
            String report = clientName + " присоединился к чату!";
            System.out.println(report);
            Server.loger(report);
            send(report);

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
                Server.loger(clientName + " покинул чат!");

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
        Server.loger(message);
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
    private static String getClientName() throws IOException {
        String report = "Жду имени пользователя";
        System.out.println(report);
        Server.loger(report);
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
    private static void sendingHistoricalMessages() throws IOException {
        Server.loger("Отправка истории сообщений...");
        out.writeUTF(Server.getChatHistory());
        out.flush();
    }
}
