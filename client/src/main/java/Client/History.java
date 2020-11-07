package Client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class History {
    String login;

    private static History hist;


    private static PrintWriter fileOut;
    InputStreamReader fileIn;
    final int counLastMess = 100;

    private static String getHistoryFilenameByLogin(String login) {
        return "history/history_" + login + ".txt";
    }

    public static void start(String login) {
        try {
            fileOut = new PrintWriter(new FileOutputStream(getHistoryFilenameByLogin(login), true),true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void stop() {
        if (fileOut != null) {
            fileOut.close();
        }
    }

    public static void writeLine(String msg) {
        fileOut.println(msg);
    }

    public static String getLast100LinesOfHistory(String login) {
        if (!Files.exists(Paths.get(getHistoryFilenameByLogin(login)))) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            List<String> historyLines = Files.readAllLines(Paths.get(getHistoryFilenameByLogin(login)));
            int startPosition = 0;
            if (historyLines.size() > 100) {
                startPosition = historyLines.size() - 100;
            }
            for (int i = startPosition; i < historyLines.size(); i++) {
                sb.append(historyLines.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

//    public void history(ClientManager clientManager) throws IOException {
//        File newFile = new File ("history/history_"+ clientManager.getLogin() + ".txt");
//        if (!newFile.exists()){
//            newFile.createNewFile();
//        }
//        fileIn = new InputStreamReader( new FileInputStream("history/history_"+ clientManager.getLogin() + ".txt"), StandardCharsets.UTF_8);
//        StringBuilder sb = new StringBuilder();
//        StringBuilder sbLastMess = new StringBuilder();
//        int count;
//        while ((count = fileIn.read()) !=-1){
//            sb.append((char) count);
//        }
//        fileIn.close();
//        String []lastMess = sb.toString().split("\n");
//        int lastMessCount = Math.min(lastMess.length, counLastMess);
//        for (int i = lastMess.length - lastMessCount; i < lastMess.length; i++) {
//            sbLastMess.append(lastMess[i] + "\n");
//        }
//        clientManager.sentMessage(sbLastMess.toString());
//        fileOut = new FileOutputStream("history/history_"+ clientManager.getLogin() + ".txt", true);
//    }
}
