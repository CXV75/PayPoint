package paypoint;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLogin implements Initializable {

    @FXML
    private Button loginButton;
    @FXML
    private Label wrongLogIn;
    @FXML
    private TextField username;
    @FXML
    private FontAwesomeIconView userClient;
    @FXML
    private PasswordField password;
    public static String loggedInUser;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234"; 

    public void login(ActionEvent event) throws IOException {
        checkLogin();
    }
    
    public void userClient(Event event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) userClient.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void checkLogin() throws IOException {
        if (username.getText().equals(ADMIN_USERNAME) && password.getText().equals(ADMIN_PASSWORD)) {
            loggedInUser = username.getText();
            PayPoint m = new PayPoint();
            m.changeScene("AfterAdminLogin.fxml");
        } else if (username.getText().isEmpty() || password.getText().isEmpty()) {
            wrongLogIn.setText("Please enter your data.");
        } else {
            wrongLogIn.setText("Wrong username or password!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}
