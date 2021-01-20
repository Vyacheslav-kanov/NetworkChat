import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{

    //Создаем сервенный сокет
    private static ServerSocket serverSocket;
    //Получаем порт из файла настроек
    private static final int port = getPort("ServerTmp\\settings.txt");
    //Список сокетов клиентов которые подключались к серверу
    private static final LinkedList<Socket> serverList = new LinkedList<>();
    //Пул потоков для диалога с клиентом
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    //Билдер истории переписки
    private static final StringBuilder chatHistory = new StringBuilder();


    public static LinkedList<Socket> getServerList() {
        return serverList;
    }

    public static void main(String[] args) {
        serverStart();
    }

    public static void serverStart() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен");
            loger("Сервер запущен\"Жду подключений...");
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                serverList.add(client);
                executorService.execute(new ServerService(client));
                loger("Клиент присоединился!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                System.out.println("Сервер прекратил работу!");
                loger("Сервер прекратил работу!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void command(String comm) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        if(reader.ready()) {
            if (comm.equals("/exit")) {
                loger("Сервер завершает работу!");
                serverSocket.close();
            }
        }
    }

    //Создает файл настроек
    private static void createFileSettings() throws IOException {
        File serverTmp = new File("ServerTmp\\");
        File settings = new File(serverTmp.getPath() + "\\settings.txt");
        if (!settings.exists()) {
            serverTmp.mkdir();
            settings.createNewFile();
            writeFile(settings.getPath(), "port:00000");
        }
    }

    //Возвращает порт из фвйла настроек
    public static int getPort(String settingsPath) {
        File settings = new File(settingsPath);
        try {
            createFileSettings();
            String mess = readFile(settings.getPath());
            String[] split = mess.split(".*port:|\\ .*");
            return Integer.parseInt(split[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Создает файл логирования с сохранением историии
    private static void createFileLog() throws IOException {
        File serverTmp = new File("ServerTmp\\");
        File chatLog = new File("ServerTmp\\chatLog.txt");
        if (!chatLog.exists()) {
            serverTmp.mkdir();
            chatLog.createNewFile();
        }
    }

    //Записывает в файл логирования сообщение
    public synchronized static void loger(String message) throws IOException {
        File chatLog = new File("ServerTmp\\chatLog.txt");
        createFileLog();
        String log = readFile(chatLog.getPath());
        writeFile(chatLog.getPath(), log + "\n" + message);
    }

    //Сохранить сообщение в историю чата
    public static synchronized void saveMessage(String message){
        chatHistory.append(message + "\n");
    }

    //Возвращает историю чата в виде строки
    public static String getChatHistory(){
        if(chatHistory.toString().isEmpty()) return "Чат пуст";
        else return chatHistory.toString();
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
