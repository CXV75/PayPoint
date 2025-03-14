package paypoint;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

public class FXMLDocumentController implements Initializable {

    @FXML
    private Button loginButton;
    @FXML
    private Label wrongLogIn;
    @FXML
    private TextField username;
    @FXML
    private FontAwesomeIconView userSecret;
    @FXML
    private FontAwesomeIconView userClient;
    @FXML
    private PasswordField password;
    public static String loggedInUser;
    
    private Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/paypoint_db", "root", "");
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    public void login(ActionEvent event) throws IOException {
        checkLogin();
    }
    
public void userSecret(Event event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminLogin.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) userSecret.getScene().getWindow();
    stage.setScene(new Scene(root));
}

public void userClient(Event event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) userClient.getScene().getWindow();
    stage.setScene(new Scene(root));
}



private void checkLogin() throws IOException {
        Connection conn = connect();
        if (conn == null) {
            wrongLogIn.setText("Database connection failed.");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = SHA2(?, 256)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.getText());
            stmt.setString(2, password.getText());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInUser = username.getText(); 
                PayPoint m = new PayPoint();
                m.changeScene("AfterLogin.fxml");
            } else if (username.getText().isEmpty() || password.getText().isEmpty()) {
                wrongLogIn.setText("Please enter your data.");
            } else {
                wrongLogIn.setText("Wrong username or password!");
            }
        } catch (Exception e) {
            wrongLogIn.setText("Error: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}
