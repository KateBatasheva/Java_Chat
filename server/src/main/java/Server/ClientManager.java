package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

public class ClientManager {
    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nick;
    private String login;

    private SimpleAuth authUser;
    private DBautheriszation authDB;

    public ClientManager(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

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
//                                authDB.fill(tocken[1], tocken[2], tocken[3]);
                            } else {
                                sentMessage(SystemCommands.registrNO.getCode());
                            }
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

                    while (true) {
                        socket.setSoTimeout(0);
                        String mess = in.readUTF();
                        if (mess.startsWith(SystemCommands.exit.getCode())) {
                            break;
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
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.castMess(this, null, "*** left chat ***\n");
                    System.out.println("Client is disconnected");
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
}



