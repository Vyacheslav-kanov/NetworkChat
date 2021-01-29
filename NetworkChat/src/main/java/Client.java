import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client extends Thread {

    private static final int port = getPort("ClientTmp/settings.txt");
    private static final String ip = getIp("ClientTmp/settings.txt");
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;

    public Client() {
        try {
            this.socket = new Socket(ip, port);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream((socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client().start();
    }

    @Override
    public void run() {
        try {
            String userName = "NoName";
            String response; //Ответ сервера
            logger("Введите имя пользователя");
            userName = reader.readLine();

            System.out.println();
            logger("Попытка подключения к серверу...");

            /**
             * Отправляем серверу имя пользователя на которое должны получить ответ
             * в качестве ответа, сервер должен отправить сообщение "name Вы присоединились к чату!"
             * Если ответ получили значит диалог установлен.
             */
            out.writeUTF(userName);
            System.out.println();
            logger("Ждем ответ сервера..."); //Каждый процесс записываем в лог.
            response = listenAnswerServer();  //Ждем пока не получим ответ.
            logger(response);

            System.out.println();
            logger("Получение сообщений...");
            response = listenAnswerServer(); //Ждем пока сервер отправит историю.
            logger(response);

            System.out.println();
            logger("Вводите сообщения:");

            /**
             * Создаем поток чтения, который будет постоянно читать сообщения параллельно потоку клиента
             * в котором он будет отправлять сообщения на сервер, а сервер разссылать всем остальным клиентам
             * у которых поток чтения будет выполнять свою работу и читать эти сообщения для клиента.
             */
            new ThreadMessagesListener(socket).start();
            while (!socket.isClosed() || isInterrupted()) {
                sendingMessages(reader.readLine());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.flush();
                out.close();
                logger("Вы вышли из чата!");
                logger("Клиент завершил работу");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendingMessages(String mess) throws IOException {
        if (mess.equals("/exit")) { //перед отправкой сообщения проверяем, евляется ли сообщение командой выхода
            socket.close();
        } else {
            out.writeUTF(mess);
            out.flush();
        }
        logger(mess);
    }

    /**
     * создание файла настроек
     * создаем обьект файл для папки и файла настроек в нем
     * проверяем существует ли файл настроек уже, чтобы случайне не перезаписать его
     * если его нет создаем с стандартными настройками
     * @throws IOException
     */
    private static void createFileSettings() throws IOException {
        File clientTmp = new File("ClientTmp/");
        File settings = new File("ClientTmp/settings.txt");
        if (!settings.exists()) {
            clientTmp.mkdir();
            settings.createNewFile();
            writeFile(settings.getPath(), "IP:localHost\nPort:0000");
        }
    }

    /**
     * Возвращает порт из файла настроек по указанному пути файла настроек
     * проверяем на наличие файла настроек
     * читаем файл и достаем числа порта из текста
     * @param settingsPath
     * @return
     */
    private static int getPort(String settingsPath) {
        File settings = new File(settingsPath);
        int result = 00000;
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] splitBuf = mess.split(";\n");
            splitBuf = splitBuf[1].split("/port:");
            result = Integer.parseInt(splitBuf[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            logger("Получен порт " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Возвращает айпи адрес из файла настроек по указанному пути файла настроек
     * проверяем на наличие файла настроек
     * читаем файл и достаем айпи адресс из текста
     * @param settingsPath
     * @return
     */
    private static String getIp(String settingsPath) {
        File settings = new File(settingsPath);
        String result = "localhost";
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] splitBuf = mess.split(";\n");
            splitBuf = splitBuf[0].split("/IP:");
            result = splitBuf[1];
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            logger("Получен адрес " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Записывает в файл логирования сообщение
     * проверяем на наличие файла логирования
     * получаем текст лога и прибовляем к нему сообщение
     * и записываем в лог
     * @param message
     * @throws IOException
     */
    public static synchronized void logger(String message) throws IOException {
        File chatLog = new File("ClientTmp/chatLog.txt");
        createFileLog();
        String log = "("
                + LocalDate.now().format(DateTimeFormatter.ofPattern("DD.MM"))
                + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ") "
                + " - " + message;
        writeFile(chatLog.getPath(),readFile(chatLog.getPath()) + "\n" + log);
        System.out.println(log);
    }

    //Создает файл логирования
    private static void createFileLog() throws IOException {
        File clientTmp = new File("ClientTmp/");
        File chatLog = new File("ClientTmp/chatLog.txt");
        if (!chatLog.exists()) {
            clientTmp.mkdir();
            chatLog.createNewFile();
        }
    }

    //Читает файл и возвращает прочитанный текст
    private static String readFile(String filePath) throws IOException {
        int i;
        String mess = "";
        FileReader reader = new FileReader(filePath);
        while ((i = reader.read()) != -1) {
            mess += String.valueOf((char) i);
        }
        return mess;
    }

    //записывает текст в файл
    private static void writeFile(String filePath, String writeText) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(writeText);
        writer.flush();
    }

    private String listenAnswerServer() throws IOException {
        String response;
        while((response = in.readUTF()).isEmpty());
        return response;
    }
}
