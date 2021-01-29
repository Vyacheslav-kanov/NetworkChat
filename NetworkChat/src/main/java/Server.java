import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{

    //Создаем сервенный сокет
    private static ServerSocket serverSocket;
    //Получаем порт из файла настроек
    private static final int port = getPort("ServerTmp/settings.txt");
    //Список сокетов клиентов которые подключались к серверу
    private static final LinkedList<Socket> serverList = new LinkedList<>();
    //Пул потоков для диалога с клиентом
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    //Билдер истории переписки
    private static final StringBuilder chatHistory = new StringBuilder();
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    public static LinkedList<Socket> getServerList() {
        return serverList;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            logger("Сервер запущен");
            logger("Жду подключений...");
            while (!serverSocket.isClosed() || isInterrupted()) {
                if(reader.ready()){
                    command(reader.readLine());
                }
                Socket client = serverSocket.accept();
                serverList.add(client);
                executorService.execute(new ServerService(client));
                logger("Клиент присоединился!");
                logger(serverList.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                logger("Сервер прекратил работу!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }

    public static void command(String comm) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        logger(comm);
        if(reader.ready()) {
            if (comm.equals("/exit")) {
                logger("Сервер завершает работу!");
                serverSocket.close();
            }
        }
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

    //Возвращает порт из фвйла настроек
    public static int getPort(String settingsPath) {
        File settings = new File(settingsPath);
        int result = 0;
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] split = mess.split(".*port:|\\ .*");
            result = Integer.parseInt(split[1]);
            logger("Получил порт " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Создает файл логирования с сохранением историии
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
        writeFile(chatLog.getPath(),readFile(chatLog.getPath()) + "\n" + log);
        System.out.println(log);
    }

    //Сохранить сообщение в историю чата
    public static synchronized void saveMessage(String message) throws IOException {
        chatHistory.append(message + "\n");
        logger("сообщение сохранено!");
    }

    //Возвращает историю чата в виде строки
    public static String getChatHistory() throws IOException {
        if(chatHistory.toString().isEmpty()){
            logger("Чат пуст");
            return "Чат пуст";
        }
        else{
            logger("Чат получен!");
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
