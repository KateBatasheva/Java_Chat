package Client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public HBox hb_sendMess;
    @FXML
    public ListView<String> lv_clients;
    @FXML
    private TextArea ta_mainField;
    @FXML
    private TextField tf_message;
    @FXML
    private MenuItem mi_close;
    @FXML
    private Button b_sent;
    @FXML
    private HBox hb_authPanel;
    @FXML
    private TextField tf_login;
    @FXML
    private PasswordField pf_password;
    @FXML
    private HBox hb_mainPanel;


    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    final String IP_ADDRESS = "localhost";
    final int Port = 8189;

    private boolean authentif;

    private String nick;
    private Stage stage;
    private Stage regStage;
    private Registration registration;

    public void setAuthentif (boolean authentif){
        this.authentif = authentif;
        hb_authPanel.setVisible(!authentif);
        hb_authPanel.setManaged(!authentif);
        hb_sendMess.setVisible(authentif);
        hb_sendMess.setManaged(authentif);
        lv_clients.setVisible(authentif);
        lv_clients.setManaged(authentif);

        if (!authentif){
            nick = "";
            setTittle("Sweeties chat");
        } else {
            setTittle(String.format("Sweeties chat - [ %s ]", nick));
        }

        ta_mainField.clear();
    }

    public void clickSent(ActionEvent actionEvent) {
        if (tf_message.getText().trim().length() ==0) {
            return;
        }try {
                out.writeUTF(tf_message.getText()  + "\n");
                tf_message.clear();
                tf_message.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void mi_close(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            Stage stage = (Stage) b_sent.getScene().getWindow();
            try {
                out.writeUTF(SystemCommands.exit.getCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.close();
        });
    }
    public void connect (){
        try {
            socket = new Socket(IP_ADDRESS, Port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // authentication step
                    while (true) {
                        String mess = in.readUTF();
                        if (mess.startsWith(SystemCommands.authok.getCode())){
                            nick = mess.split("\\s")[1];
                            setAuthentif(true);
                            break;
                        }
                        if (mess.startsWith(SystemCommands.registrOK.getCode())){
                            registration.addMess("Registration success\n");
                        }
                        if (mess.startsWith(SystemCommands.registrNO.getCode())){
                            registration.addMess("Registration fail\n Login or nick is used\n");
                        }
                        if (mess.startsWith(SystemCommands.timeout.getCode())){
                            ta_mainField.appendText("Timeout connection\n");
                        }
                    }
                    // work step
                    while (true) {
                        String mess = in.readUTF();
                        if (mess.startsWith("/")){
                            if (mess.startsWith(SystemCommands.exit.getCode())){
                                break;
                        }
                            // 2.*Добавить в сетевой чат возможность смены ника.
                            if (mess.startsWith(SystemCommands.changeNick.getCode())){
                                String [] tockens = mess.split("\\s");
                                setTittle(String.format("Sweeties chat - [ %s ]", tockens[1]));
                            }
                            if (mess.startsWith(SystemCommands.clients.getCode())){
                                String [] tockens = mess.split("\\s");
                                Platform.runLater(()->
                                {
                                    lv_clients.getItems().clear();
                                    for (int i = 1; i <tockens.length ; i++) {
                                        lv_clients.getItems().add(tockens[i]);
                                    }
                                });
                            }

                            } else {
                            ta_mainField.appendText(mess);
                        }
                    }
                }  catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthentif(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> {
            stage = ((Stage) ta_mainField.getScene().getWindow());
            stage.setOnCloseRequest(event -> {
                if (socket != null && !socket.isClosed()){
                    try {
                        out.writeUTF(SystemCommands.exit.getCode());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthentif(false);
        regWindow();
    }

    private void regWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root  = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 500, 350));
            regStage.initModality(Modality.APPLICATION_MODAL);

            registration = fxmlLoader.getController();
            registration.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tryToEnter(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
            String mesServ = String.format("/auth %s %s", tf_login.getText().trim(), pf_password.getText().trim());
            try {
                out.writeUTF(mesServ);
                pf_password.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
private void setTittle (String tittle){
        Platform.runLater(()-> {
            stage.setTitle(tittle);
        });
}

    public void clickPrivat(MouseEvent mouseEvent) {
        tf_message.setText(String.format("%s %s ", SystemCommands.write.getCode(), lv_clients.getSelectionModel().getSelectedItem()));
    }

    public void regWindowShow (ActionEvent actionEvent) {
        regStage.show();

    }

    public void tryRegistr (String login, String password, String nick){
        String mes = String.format("%s %s %s %s", SystemCommands.register.getCode(), login, password, nick);

        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF(mes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
