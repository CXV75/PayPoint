package paypoint;

import static com.mysql.cj.telemetry.TelemetryAttribute.DB_USER;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.json.JSONObject;

public class AfterLogin implements Initializable {  
    @FXML
    private Button logoutButton, recordButton;
    @FXML
    private TextField totalAmountField, totalDueField, totalMoneyField, totalTransactionsField, totalProfitField;
    @FXML
    private Spinner<Integer>[] spinners = new Spinner[9];  
    @FXML
    private Spinner mySpinner, mySpinner1, mySpinner2, mySpinner3, mySpinner4, mySpinner5, mySpinner6, mySpinner7, mySpinner8;
    @FXML
    private TextArea changeArea, promptArea, responseArea, helpArea, helpArea1, helpArea2, helpArea3, helpArea4;
    @FXML
    private FontAwesomeIconView resetButton, reloadButton;
    @FXML
    private TableView<Denomination> denominationTable, denominationTable1;
    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private PieChart pieChart;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private ComboBox<String> intervalComboBox;
    @FXML
    private TableColumn<Denomination, Integer> denominationColumn, quantityColumn, denominationColumn1, quantityColumn1, transactionIDColumn, totalDueColumn, totalAmountColumn, totalChangeColumn, transactDateColumn;    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/paypoint_db"; 
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    private String cashier;
    @FXML
    private void handleRunOllama(ActionEvent event) {
        String inputText = promptArea.getText();  
        responseArea.setText("Loading...");
        Task<String> ollamaTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    return OllamaIntegration.runOllamaModel(inputText);
                } catch (IOException e) {
                    return "Error: " + e.getMessage();
                }
            }
        };
            promptArea.setText(""); 
            ollamaTask.setOnSucceeded(event1 -> {
                responseArea.setText(ollamaTask.getValue()); 
            });

            ollamaTask.setOnFailed(event1 -> {
                responseArea.setText("Error: " + ollamaTask.getException().getMessage());
            });

            new Thread(ollamaTask).start();
        }

        int totalAmount, totalDue, totalChange, count;
        int[] denominations = {1000, 500, 200, 100, 50, 20, 10, 5, 1};
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        totalDueField.setText("0");
        intervalComboBox.getItems().addAll("Hours", "Days", "Weeks", "Months");
        intervalComboBox.setOnAction(event -> populateLineChart(intervalComboBox.getValue()));
        cashier = FXMLDocumentController.loggedInUser;  
        responseArea.setEditable(false);
        changeArea.setEditable(false);
        totalAmountField.setEditable(false);
        totalMoneyField.setEditable(false);
        helpArea.setEditable(false);
        helpArea1.setEditable(false);
        helpArea2.setEditable(false);
        helpArea3.setEditable(false);
        helpArea4.setEditable(false);
        denominationColumn.setCellValueFactory(new PropertyValueFactory<>("denomination"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        denominationColumn1.setCellValueFactory(new PropertyValueFactory<>("denomination"));
        quantityColumn1.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        transactionIDColumn.setCellValueFactory(new PropertyValueFactory<>("transactionID"));
        totalDueColumn.setCellValueFactory(new PropertyValueFactory<>("totalDue"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalChangeColumn.setCellValueFactory(new PropertyValueFactory<>("totalChange"));
        transactDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        
        populatePieChart();
        loadTransactionTable();
        loadDenominationInventory();
        
        spinners[0] = mySpinner;
        spinners[1] = mySpinner1;
        spinners[2] = mySpinner2;
        spinners[3] = mySpinner3;
        spinners[4] = mySpinner4;
        spinners[5] = mySpinner5;
        spinners[6] = mySpinner6;
        spinners[7] = mySpinner7;
        spinners[8] = mySpinner8;
        
        for (Spinner<Integer> spinner : spinners) {
            if (spinner != null) {
                spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100));
                spinner.setDisable(true);
            }
        }

        totalDueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || !newValue.matches("\\d+")) {
                disableSpinners(true); 
            } else {
                disableSpinners(false);
            }
        });

        for (Spinner<Integer> spinner : spinners) {
            spinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { 
                    spinner.increment(0); 
                    try {
                        calculate(null);
                    } catch (IOException ex) {
                        Logger.getLogger(AfterLogin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
        
        totalDueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { 
                try {
                    calculate(null); 
                } catch (IOException ex) {
                    Logger.getLogger(AfterLogin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        promptArea.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleRunOllama(new ActionEvent()); 
                promptArea.getParent().requestFocus();
            }
        });
    }
    
    private Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void populatePieChart() {
        String query = "SELECT denomination, quantity FROM denominations";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            pieChart.getData().clear();

            while (rs.next()) {
                int denomination = rs.getInt("denomination"); 
                int count = rs.getInt("quantity");

                pieChart.getData().add(new PieChart.Data("₱" + denomination, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



private void populateLineChart(String interval) {
    String query = "";
    switch (interval) {
        case "Hours":
            query = "SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d %H:00:00') AS time_period, " +
                    "COUNT(*) AS transaction_count, SUM(total_amount - total_due) AS total_profit " +
                    "FROM transactions GROUP BY time_period ORDER BY time_period";
            break;
        case "Days":
            query = "SELECT DATE(transaction_date) AS time_period, COUNT(*) AS transaction_count, " +
                    "SUM(total_amount - total_due) AS total_profit FROM transactions GROUP BY time_period ORDER BY time_period";
            break;
        case "Weeks":
            query = "SELECT YEARWEEK(transaction_date) AS time_period, COUNT(*) AS transaction_count, " +
                    "SUM(total_amount - total_due) AS total_profit FROM transactions GROUP BY time_period ORDER BY time_period";
            break;
        case "Months":
            query = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') AS time_period, COUNT(*) AS transaction_count, " +
                    "SUM(total_amount - total_due) AS total_profit FROM transactions GROUP BY time_period ORDER BY time_period";
            break;
    }

    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transaction Count");

        int totalTransactions = 0;
        double totalProfit = 0.0;

        while (rs.next()) {
            String timePeriod = rs.getString("time_period");
            int transactionCount = rs.getInt("transaction_count");
            double profit = rs.getDouble("total_profit");

            totalTransactions += transactionCount;
            totalProfit += profit;

            series.getData().add(new XYChart.Data<>(timePeriod, transactionCount));
        }

        lineChart.getData().add(series);

        CategoryAxis xAxis = (CategoryAxis) lineChart.getXAxis();
        xAxis.setLabel("");
        xAxis.setTickLabelRotation(0);
        xAxis.getCategories().clear();

        totalTransactionsField.setText(String.valueOf(totalTransactions));
        totalProfitField.setText(String.format("₱ %.2f", totalProfit));

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void loadDenominationInventory() {
        ObservableList<Denomination> inventoryList = FXCollections.observableArrayList();
        int totalMoney = 0;

        String query = "SELECT denomination, quantity FROM denominations ORDER BY denomination DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int denomination = rs.getInt("denomination");
                int quantity = rs.getInt("quantity");

                inventoryList.add(new Denomination(denomination, quantity));
                totalMoney += denomination * quantity;
            }
            denominationTable.setItems(inventoryList);
            denominationTable1.setItems(inventoryList);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalMoneyField.setText("₱ " + totalMoney);
        
    }
    
    public static class Denomination {
        private final Integer denomination;
        private final Integer quantity;

        public Denomination(Integer denomination, Integer quantity) {
            this.denomination = denomination;
            this.quantity = quantity;
        }

        public Integer getDenomination() { return denomination; }
        public Integer getQuantity() { return quantity; }
    }
    
    public void recordTransact(Event event) throws IOException {

        int totalAmount = Integer.parseInt(totalAmountField.getText());
        String totalDueText = totalDueField.getText().trim();

        int totalDue = totalDueText.isEmpty() ? 0 : Integer.parseInt(totalDueText);

        if (totalDue == 0 || totalAmount < totalDue) {
            changeArea.setText("Transaction cannot proceed: Total Due is 0 or Tendered Amount is less than Total Due");
            return; 
        }

        int totalChange = totalAmount - totalDue;

        String url = "jdbc:mysql://localhost:3306/paypoint_db";
        String user = "root";
        String password = ""; 


        String cashierName = cashier;
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String insertTransactionSQL = "INSERT INTO transactions (cashier, total_amount, total_due, total_change, transaction_date) VALUES (?, ?, ?, ?, ?)";
        String updateDenominationSQL = "UPDATE denominations SET quantity = quantity + ? WHERE denomination = ?";
        String deductDenominationSQL = "UPDATE denominations SET quantity = quantity - ? WHERE denomination = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionSQL);
             PreparedStatement updateDenominationStmt = conn.prepareStatement(updateDenominationSQL);
             PreparedStatement deductDenominationStmt = conn.prepareStatement(deductDenominationSQL)) {

            conn.setAutoCommit(false); 

            insertTransactionStmt.setString(1, cashierName);
            insertTransactionStmt.setInt(2, totalAmount);
            insertTransactionStmt.setInt(3, totalDue);
            insertTransactionStmt.setInt(4, totalChange);
            insertTransactionStmt.setString(5, formattedDate);
            insertTransactionStmt.executeUpdate();

            for (int i = 0; i < denominations.length; i++) {
                int denomValue = denominations[i];
                int denomCount = spinners[i].getValue();

                if (denomCount > 0) {
                    updateDenominationStmt.setInt(1, denomCount);
                    updateDenominationStmt.setInt(2, denomValue);
                    updateDenominationStmt.executeUpdate();
                }
            }

            Map<Integer, Integer> changeMap = calculateChange(totalChange);
            for (Map.Entry<Integer, Integer> entry : changeMap.entrySet()) {
                int denomValue = entry.getKey();
                int denomCount = entry.getValue();

                if (denomCount > 0) {
                    deductDenominationStmt.setInt(1, denomCount);
                    deductDenominationStmt.setInt(2, denomValue);
                    deductDenominationStmt.executeUpdate();
                }
            }

            conn.commit(); 
            loadTransactionTable();
            loadDenominationInventory();
            changeArea.setText("Transaction Recorded Successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            changeArea.setText("Error recording transaction: " + e.getMessage());
        }
    }

    public void resetField(Event event) throws IOException{
        totalDueField.setText("");
        totalAmountField.setText("");
        changeArea.setText("");
        mySpinner.getValueFactory().setValue(0);
        mySpinner1.getValueFactory().setValue(0);
        mySpinner2.getValueFactory().setValue(0);
        mySpinner3.getValueFactory().setValue(0);
        mySpinner4.getValueFactory().setValue(0);
        mySpinner5.getValueFactory().setValue(0);
        mySpinner6.getValueFactory().setValue(0);
        mySpinner7.getValueFactory().setValue(0);
        mySpinner8.getValueFactory().setValue(0);
    }

    private void disableSpinners(boolean disable) {
        for (Spinner<Integer> spinner : spinners) {
            spinner.setDisable(disable);
        }
    }

    public void calculate(Event event) throws IOException {
        totalAmount = 0;
        totalChange = 0;
        count = 0;

        for (Spinner<Integer> spinner : spinners) {
            totalAmount += spinner.getValue() * denominations[count];
            count++;
        }
        totalAmountField.setText(String.valueOf(totalAmount));

        String dueText = totalDueField.getText().trim();
        if (dueText.isEmpty()) {
            totalDue = 0;
        } else {
            try {
                totalDue = Integer.parseInt(dueText);
            } catch (NumberFormatException e) {
                totalDueField.setText("0");
                totalDue = 0;
            }
        }
        totalChange = totalAmount - totalDue;
        if (totalDue > totalAmount) {
            changeArea.setText("Invalid Amount.");
        } else {
            calculateChange(totalChange);
        }
    }
    
    private void loadTransactionTable() {
    ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    String query = "SELECT transaction_id, total_due, total_amount, total_change, transaction_date FROM transactions ORDER BY transaction_id ASC";

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            int transactionID = rs.getInt("transaction_id");
            int totalDue = rs.getInt("total_due");
            int totalAmount = rs.getInt("total_amount");
            int totalChange = rs.getInt("total_change");
            String transactionDate = rs.getString("transaction_date");

            transactionList.add(new Transaction(transactionID, totalDue, totalAmount, totalChange, transactionDate));
        }
        transactionTable.setItems(transactionList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Transaction {
        private final Integer transactionID;
        private final Integer totalDue;
        private final Integer totalAmount;
        private final Integer totalChange;
        private final String transactionDate;

        public Transaction(Integer transactionID, Integer totalDue, Integer totalAmount, Integer totalChange, String transactionDate) {
            this.transactionID = transactionID;
            this.totalDue = totalDue;
            this.totalAmount = totalAmount;
            this.totalChange = totalChange;
            this.transactionDate = transactionDate;
        }

        public Integer getTransactionID() { return transactionID; }
        public Integer getTotalDue() { return totalDue; }
        public Integer getTotalAmount() { return totalAmount; }
        public Integer getTotalChange() { return totalChange; }
        public String getTransactionDate() { return transactionDate; }
    }
    
    private Map<Integer, Integer> calculateChange(int totalChange) {
        Map<Integer, Integer> changeMap = new LinkedHashMap<>();
        StringBuilder changeBreakdown = new StringBuilder("Change Breakdown:\n");
        int originalChange = totalChange;

        for (int denom : denominations) {
            if (totalChange >= denom) {
                int count = totalChange / denom;
                totalChange %= denom;
                changeMap.put(denom, count);
                changeBreakdown.append("₱").append(denom).append(" X ").append(count).append("\n");
            }
        }

        changeBreakdown.append("Total Change: ₱").append(originalChange);

        if (changeMap.isEmpty()) {
            changeArea.setText("No change needed.");
        } else {
            changeArea.setText(changeBreakdown.toString());
        }

        return changeMap;
    }


    
    public void logout(ActionEvent event) throws IOException {
        PayPoint m = new PayPoint();
        m.changeScene("FXMLDocument.fxml");
    }

    public static class OllamaIntegration {
        public static String runOllamaModel(String promptText) throws IOException {
            String modelName = "llama3.2:1b"; 
            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", modelName, promptText
            );
            conn.getOutputStream().write(jsonInputString.getBytes("UTF-8"));
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("response");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    errorResponse.append(line);
                }
                in.close();
                return "Error: " + errorResponse.toString();
            }
        }
    }
}
