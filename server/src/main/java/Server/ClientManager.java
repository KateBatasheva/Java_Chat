package Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.*;

public class ClientManager {

    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nick;
    private String login;

    FileOutputStream fileOut;
    InputStreamReader fileIn;

//    History history;

    final int counLastMess = 100;

    public ClientManager(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
//            fileHandler = new FileHandler("log.txt",true);
//            fileHandler.setFormatter(new SimpleFormatter());
//            fileHandler.setLevel(Level.INFO);

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);

                    // authentication step
                    while (true) {
                        String mess = in.readUTF();
                        if (mess.startsWith(SystemCommands.register.getCode())) {
                            String[] tocken = mess.split("\\s");
                            if (tocken.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuth().registr(tocken[1], tocken[2], tocken[3]);
                            if (b) {
                                sentMessage(SystemCommands.registrOK.getCode());
                                // ----- //
                                server.getLogger().info("Client " + socket.getRemoteSocketAddress() + " is registered with nick " + tocken[3]);
                                // ----- //
                            } else {
                                sentMessage(SystemCommands.registrNO.getCode());
                                // ----- //
                                server.getLogger().info("Fail register for client " + socket.getRemoteSocketAddress() + " with nick " + tocken[3]);
                                // ----- //
                            }
                        }
                        if (mess.startsWith(SystemCommands.deleteUsers.getCode())){
                            DBManager.deleteUsers();
                            deleteAllFilesFolder ("history");
                        }

                        if (mess.startsWith(SystemCommands.auth.getCode())) {
                            String[] tocken = mess.split("\\s");
                            if (tocken.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuth().getNick(tocken[1], tocken[2]);
                            if (newNick != null) {
                                login = tocken[1];
                                if (!server.isAuth(login)) {
                                    nick = newNick;
                                    sentMessage(SystemCommands.authok.getCode() + " " + newNick);
                                    server.subscribe(this);

//                                    History hist = new History();
//                                    hist.history(this);
//                                    history.createHistory().history(this);
//                                    File newFile = new File ("history/history_"+ this.getLogin() + ".txt");
//                                    if (!newFile.exists()){
//                                        newFile.createNewFile();
//                                    }
//                                    fileIn = new InputStreamReader( new FileInputStream("history/history_"+ this.getLogin() + ".txt"), StandardCharsets.UTF_8);
//                                    StringBuilder sb = new StringBuilder();
//                                    StringBuilder sbLastMess = new StringBuilder();
//                                    int count;
//                                    while ((count = fileIn.read()) !=-1){
//                                            sb.append((char) count);
//                                        }
//                                    fileIn.close();
//                                    String []lastMess = sb.toString().split("\n");
//                                    int lastMessCount = Math.min(lastMess.length, counLastMess);
//                                    for (int i = lastMess.length - lastMessCount; i < lastMess.length; i++) {
//                                        sbLastMess.append(lastMess[i] + "\n");
//                                    }
//                                    this.sentMessage(sbLastMess.toString());
//                                    fileOut = new FileOutputStream("history/history_"+ this.getLogin() + ".txt", true);
                                    server.castMess(this, null, "*** join chat ***\n");
                                    break;
                                } else {
                                    sentMessage("Login is already used");
                                }
                            } else {
                                sentMessage("invalid login/ password");
                            }
                        }
                    }
                    // work step
                    socket.setSoTimeout(0);
                    while (true) {
                        String mess = in.readUTF();
                        if (mess.startsWith(SystemCommands.exit.getCode())) {
                            break;
                        }
                        if (mess.startsWith(SystemCommands.changeNick.getCode())) {
                            String[] newNick = mess.split("\\s");
                            if (newNick.length != 2) {
                                mess = "Invalid request or a new nick contains spaces";
                                // ----- //
                                server.getLogger().severe("Failed to change nick for Client " + socket.getRemoteSocketAddress());
                                // ----- //
                            } else {
                                if (server.getAuth().changeNick(this.getNickname(), newNick[1])) {
                                sentMessage(SystemCommands.changeNick.getCode() + " " + newNick[1]);
                                mess = this.getNickname() +" change nick to " + newNick[1];
                                    this.nick = newNick[1];
                                    server.castClients();
                                } else {
                                    mess = "Failed to change nick. Try again.";
                                    // ----- //
                                    server.getLogger().severe("Failed to change nick for Client " + socket.getRemoteSocketAddress());
                                    // ----- //
                                }
                            }
                        }
                        ClientManager receiver = null;
                        if (mess.startsWith(SystemCommands.write.getCode())) {
                            String[] privat = mess.split("\\s");
                            receiver = server.getClient(privat[1]);
                            if (receiver == null) {
                                mess = "Invalid nickname or request";
                                ClientManager.this.sentMessage(mess);
                                continue;
                            }
                        }
                        server.castMess(this, receiver, mess);
                    }
                } catch (SocketTimeoutException a) {
                    sentMessage(SystemCommands.exit.getCode());
                    sentMessage(SystemCommands.timeout.getCode());
                    a.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    try {
                        server.castMess(this, null, "*** left chat ***\n");
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Client is disconnected, name "+ socket.getRemoteSocketAddress());
                    // ----- //
                    server.getLogger().severe("Client is disconnected, name "+ socket.getRemoteSocketAddress());
                    // ----- //
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sentMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public static void deleteAllFilesFolder(String path) {
        for (File myFile : new File(path).listFiles())
            if (myFile.isFile()) myFile.delete();
    }
}



