import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.System.out;

public class Client extends Thread {

    private static final int port = getPort("ClientTmp\\settings.txt");
    private static final String ip = getIp("ClientTmp\\settings.txt");
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        new Client().start();
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String report; //Создал переменную, чтобы не повторять одну и туже строку в коде.
            String userName = "NoName";

            report = "Введите имя пользователя";
            System.out.println(report);
            userName = reader.readLine();
            loger(report);

            report = "Попытка подключения к серверу...";
            System.out.println(report);
            loger(report);

            String serverAnswer; //Ответ сервера

            /**
             * Отправляем серверу имя пользователя на которое должны получить ответ
             * в качестве ответа, сервер должен отправить сообщение "name Вы присоединились к чату!"
             * Если ответ получили значит диалог установлен.
             */
            out.writeUTF(userName);
            report = "\nЖдем ответ сервера...";
            System.out.println(report);
            loger(report); //Каждый процесс записываем в лог.
            serverAnswer = listenAnswerServer(socket);  //Ждем пока не получим ответ.
            System.out.println(serverAnswer);

            report = "\nПолучение сообщений..."; //Получаем историю сообщений.
            System.out.println(report);
            loger(report);
            serverAnswer = listenAnswerServer(socket); //Ждем пока сервер отправит историю.
            System.out.println(serverAnswer);

            report = "\nВводите сообщения:";
            System.out.println(report);
            loger(report);

            /**
             * Создаем поток чтения, который будет постоянно читать сообщения параллельно потоку клиента
             * в котором он будет отправлять сообщения на сервер, а сервер разссылать всем остальным клиентам
             * у которых поток чтения будет выполнять свою работу и читать эти сообщения для клиента.
             */
            new ThreadMessagesListener(socket).start();
            while (!socket.isClosed()) {
                sendingMessages(reader.readLine(), socket);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.println("Вы вышли из чата");
            out.println("Клиент прекратил работу!");
        }
    }

    private static void sendingMessages(String mess, Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        if (mess.equals("/exit")) { //перед отправкой сообщения проверяем, евляется ли сообщение командой выхода
            socket.close();
        } else {
            out.writeUTF(mess);
            out.flush();
        }
    }

    /**
     * создание файла настроек
     * создаем обьект файл для папки и файла настроек в нем
     * проверяем существует ли файл настроек уже, чтобы случайне не перезаписать его
     * если его нет создаем с стандартными настройками
     * @throws IOException
     */
    private static void createFileSettings() throws IOException {
        File clientTmp = new File("ClientTmp\\");
        File settings = new File("ClientTmp\\settings.txt");
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
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] splitBuf = mess.split(";\n");
            splitBuf = splitBuf[1].split("/port:");
            return Integer.parseInt(splitBuf[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] splitBuf = mess.split(";\n");
            splitBuf = splitBuf[0].split("/IP:");
            return splitBuf[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Записывает в файл логирования сообщение
     * проверяем на наличие файла логирования
     * получаем текст лога и прибовляем к нему сообщение
     * и записываем в лог
     * @param logText
     * @throws IOException
     */
    public static synchronized void loger(String logText) throws IOException {
        File chatLog = new File("ClientTmp\\chatLog.txt");
        createChatLog();
        String log = readFile(chatLog.getPath());
        writeFile(chatLog.getPath(), log + "\n" + logText);
    }

    //Создает файл логирования
    private static void createChatLog() throws IOException {
        File clientTmp = new File("ClientTmp\\");
        File chatLog = new File("ClientTmp\\chatLog.txt");
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

    private static String listenAnswerServer(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String answer;
        while((answer = in.readUTF()).isEmpty());
        return answer;
    }
}
