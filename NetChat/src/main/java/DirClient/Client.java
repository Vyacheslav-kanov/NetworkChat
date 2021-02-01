package DirClient;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client extends Thread {

    private final int port = getPort("ClientTmp/settings.txt");
    private final String ip = getIp("ClientTmp/settings.txt");
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private boolean powerLogger = true;


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
            String userName;
            String response; //Ответ сервера
            logger("Write user name:");
            userName = reader.readLine();

            System.out.println();
            logger("attempt to connect to the server...");

            /**
             * Отправляем серверу имя пользователя на которое должны получить ответ
             * в качестве ответа, сервер должен отправить сообщение "name* Вы присоединились к чату!"
             * Если ответ получили значит диалог установлен.
             */
            out.writeUTF(userName);
            System.out.println();
            logger("wait response from server..."); //Каждый процесс записываем в лог.
            response = listenAnswerServer();  //Ждем пока не получим ответ.
            logger(response);

            System.out.println();
            logger("getting message...");
            response = listenAnswerServer(); //Ждем пока сервер отправит историю.
            logger(response);

            System.out.println();
            logger("write message:");

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
                logger("You out from chat");
                logger("Client is close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Команды клиенту
    public void command(String comm) throws IOException {
        if (comm.equals("/exit")) {
            logger(comm);
            socket.close();
        }else if(comm.equals("/logOF")) {
            powerLogger = false;
            logger(comm);
        }else if(comm.equals("/logON")) {
            powerLogger = true;
            logger(comm);
        }
    }

    /**
     * Отправляет сообщение на сервер предварительно проверив на команду
     * @param mess
     * @throws IOException
     */
    private void sendingMessages(String mess) throws IOException {
        command(mess);
        out.writeUTF(mess);
        out.flush();
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
    public static int getPort(String settingsPath) {
        File settings = new File(settingsPath);
        int port  = 0;
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            int indexStart = mess.indexOf("port") + 5;
            int indexEnd = indexStart;
            while(!Character.toString(mess.charAt(indexEnd)).equals(";")) indexEnd++;
            String strPort = mess.substring(indexStart++, indexEnd--);
            port = Integer.parseInt(strPort);
            logger("Get port - " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    /**
     * Возвращает айпи адрес из файла настроек по указанному пути
     * проверяем на его наличие
     * читаем файл и достаем айпи адресс из текста
     * @param settingsPath
     * @return
     */
    public static String getIp(String settingsPath) {
        File settings = new File(settingsPath);
        String ip = "localhost";
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            int indexStart = mess.indexOf("IP") + 3;
            int indexEnd = indexStart;
            while(!Character.toString(mess.charAt(indexEnd)).equals(";")) indexEnd++;
            ip = mess.substring(indexStart++, indexEnd--);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            logger("Get ip address - " + ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * Записывает в файл логирования сообщение
     * проверяем на наличие файла логирования
     * получаем текст лога и прибовляем к нему сообщение
     * и записываем в лог
     * @param message
     * @throws IOException
     */
    public static void logger(String message) throws IOException {
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

    /**
     * бесконечно ждет ответа с сервера
     * дожидается и возвращает его
     * @return
     * @throws IOException
     */
    private String listenAnswerServer() throws IOException {
        String response;
        while((response = in.readUTF()).isEmpty());
        return response;
    }
}
