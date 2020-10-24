package Client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class Registration {
    @FXML public TextField tf_loginField;
    @FXML public PasswordField pf_password;
    @FXML public TextField tf_nickField;
    @FXML public Button B_reg;
    @FXML public TextArea ta_mainmessage;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void clickReg(MouseEvent mouseEvent) {
        String login = tf_loginField.getText().trim();
        String password = pf_password.getText().trim();
        String nick = tf_nickField.getText().trim();
        controller.tryRegistr(login, password, nick);
    }

    public void addMess (String message){
        ta_mainmessage.appendText(message);
    }
}
