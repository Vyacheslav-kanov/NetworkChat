package DirServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{

    //Создаем сервенный сокет
    private static ServerSocket serverSocket;
    //Получаем порт из файла настроек
    private static boolean powerLogger = true; //Для включения или отключения логера
    private static final int port = getPort("ServerTmp/settings.txt");
    //Список сокетов клиентов которые подключались к серверу
    private static final ArrayList<Socket> serverList = new ArrayList<>();
    //Пул потоков для диалога с клиентом
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    //Билдер истории переписки
    private static StringBuilder chatHistory = new StringBuilder();
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    public static ArrayList<Socket> getServerList() {
        return serverList;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            logger("Server is running");
            logger("Wait connect...");
            while (!serverSocket.isClosed()) {
                if(reader.ready()){
                    command(reader.readLine());
                }
                Socket client = serverSocket.accept();
                serverList.add(client);
                executorService.execute(new ServerService(client));
                logger("Client is join!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                logger("Server is close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }

    //Команды для сервера
    public static void command(String comm) throws IOException {
        logger(comm);
        if (comm.equals("/exit")) {
            serverSocket.close();
        }

        if(comm.equals("/clearChat")) chatHistory.delete(0, getChatHistory().length());
        if(comm.equals("/logOF")) powerLogger = false;
        if(comm.equals("/logON")) powerLogger = true;
    }

    //Создает файл настроек
    private static void createFileSettings() throws IOException {
        File serverTmp = new File("ServerTmp/");
        File settings = new File(serverTmp.getPath() + "/settings.txt");
        if (!settings.exists()) {
            serverTmp.mkdir();
            settings.createNewFile();
            writeFile(settings.getPath(), "port:00000");
        }
        logger(settings.getPath() + " created!");
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
        int port = 0;
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
     * Создает файл логирования с сохранением предыдущего лога
     * @throws IOException
     */
    private static void createFileLog() throws IOException {
        File serverTmp = new File("ServerTmp/");
        File chatLog = new File("ServerTmp/chatLog.txt");
        if (!chatLog.exists()) {
            serverTmp.mkdir();
            chatLog.createNewFile();
        }
    }

    //Записывает в файл логирования сообщение
    public synchronized static void logger(String message) throws IOException {
        File chatLog = new File("ServerTmp/chatLog.txt");
        createFileLog();
        String log = "("
                + LocalDate.now().format(DateTimeFormatter.ofPattern("DD.MM"))
                + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ") "
                + " - " + message;
        if(powerLogger) {
            writeFile(chatLog.getPath(), readFile(chatLog.getPath()) + "\n" + log);
            System.out.println(log);
        }
    }

    //Сохранить сообщение в историю чата
    public static synchronized void saveMessage(String message) throws IOException {
        chatHistory.append(message + "\n");
        logger("Message saved");
    }

    //Возвращает историю чата в виде строки
    public static String getChatHistory() throws IOException {
        if(chatHistory.toString().isEmpty()){
            logger("Chat is empty");
            return "Chat is empty";
        }
        else{
            logger("Chat received!");
            return chatHistory.toString();
        }
    }

    //Читает файл и возвращает прочитанный текст
    private static String readFile(String filePath) throws IOException {
        int i;
        String mess = "";
        FileReader reader = new FileReader(filePath);
        while((i = reader.read()) != - 1){
            mess += String.valueOf((char)i);
        }
        return mess;
    }

    //записывает текст в файл
    private static void writeFile(String filePath, String writeText) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(writeText);
        writer.flush();
    }
}
