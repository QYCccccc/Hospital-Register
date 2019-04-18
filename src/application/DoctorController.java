package application;

import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    public static final class Register {
        private SimpleStringProperty number;
        private SimpleStringProperty patientName;
        private SimpleStringProperty regTime;
        private SimpleStringProperty regType;

        Register(String number, String patientName, Timestamp regTime, boolean regType) {
            this.number = new SimpleStringProperty(number);
            this.patientName = new SimpleStringProperty(patientName);
            this.regTime = new SimpleStringProperty(regTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            this.regType = new SimpleStringProperty(regType? "专家号":"普通号");
        }
    }

    public static final class Income {
        private SimpleStringProperty deptName;
        private SimpleStringProperty doctId;
        private SimpleStringProperty doctName;
        private SimpleStringProperty regType;
        private SimpleStringProperty regCount;
        private SimpleStringProperty totalIncome;

        public Income(String deptName, String doctId, String doctName,
                      boolean regType, int regCount, double totalIncome) {
            this.deptName = new SimpleStringProperty(deptName);
            this.doctId = new SimpleStringProperty(doctId);
            this.doctName = new SimpleStringProperty(doctName);
            this.regType = new SimpleStringProperty(regType? "专家号" : "普通号");
            this.regCount = new SimpleStringProperty(Integer.toString(regCount));
            this.totalIncome = new SimpleStringProperty(String.format("%.2f", totalIncome));
        }
    }
    public static String name;
    public static String doctNum;
    @FXML
    private Label labelInfo;
    @FXML
    private Button buttonExit,buttonRefresh;
    @FXML
    private TableView table_reg, table_income;
    @FXML
    private TableColumn<Register, String> reg_id,
            patient_name,
            reg_time,
            regTab_reg_type;
    @FXML
    private TableColumn<Income, String> dept_name,
            doct_id,
            doct_name,
            incomeTab_reg_type,
            reg_count,
            totalincome;
    @FXML
    private Tab tabReg, tabIncome;
    @FXML
    private TabPane tabPane;
    @FXML
    private CheckBox checkBox_allTime, checkBox_today;
    @FXML
    private DatePicker startDate, endDate;

    private ObservableList<Register> listReg = FXCollections.observableArrayList();
    private ObservableList<Income> listIncome = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXRobotHelper.getStages().get(0).setTitle("");
        labelInfo.setText("欢迎, " + name);

        buttonExit.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Scene scene = null;
                try {
                    scene = new Scene(FXMLLoader.load(getClass().getResource("/view/Login.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FXRobotHelper.getStages().get(0).setScene(scene);
            }
        });
        buttonExit.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Scene scene = null;
                try {
                    scene = new Scene(FXMLLoader.load(getClass().getResource("/view/Login.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FXRobotHelper.getStages().get(0).setScene(scene);
            }
        });

        checkBox_allTime.setOnAction(ActionEvent -> {
            if (checkBox_allTime.isSelected()) {
                checkBox_today.setSelected(false);
                startDate.setDisable(true);
                endDate.setDisable(true);
            }
            else {
                startDate.setDisable(false);
                endDate.setDisable(false);
            }
        });
        checkBox_today.setOnAction(ActionEvent -> {
            if(checkBox_today.isSelected()) {
                checkBox_allTime.setSelected(false);
                startDate.setDisable(true);
                endDate.setDisable(true);
            }
            else {
                startDate.setDisable(false);
                endDate.setDisable(false);
            }
        });
        buttonRefresh.setOnAction(ActionEvent -> {
            on_push_buttonRefresh();
        });

        //将register对象的某些属性bind到挂号列表的特定列
        reg_id.setCellValueFactory(cellData -> cellData.getValue().number);
        patient_name.setCellValueFactory(cellData -> cellData.getValue().patientName);
        reg_time.setCellValueFactory(cellData -> cellData.getValue().regTime);
        regTab_reg_type.setCellValueFactory(cellData -> cellData.getValue().regType);

        //将Income对象的某些属性bind到收入列表的特定列
        dept_name.setCellValueFactory(cellData -> cellData.getValue().deptName);
        doct_id.setCellValueFactory(cellData -> cellData.getValue().doctId);
        doct_name.setCellValueFactory(cellData -> cellData.getValue().doctName);
        incomeTab_reg_type.setCellValueFactory(cellData -> cellData.getValue().regType);
        reg_count.setCellValueFactory(cellData -> cellData.getValue().regCount);
        totalincome.setCellValueFactory(cellData -> cellData.getValue().totalIncome);
    }
    //更新按钮的事件处理函数
    private void on_push_buttonRefresh() {
        if (tabPane.getSelectionModel().getSelectedItem() == tabReg) {
            ResultSet result = null;

            if(checkBox_allTime.isSelected()) {
                result = DBConnector.getInstance().getRegisterForDoctor(doctNum,
                        "0000-00-00 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            else if (checkBox_today.isSelected()) {
                result = DBConnector.getInstance().getRegisterForDoctor(doctNum,
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            else {
                result = DBConnector.getInstance().getRegisterForDoctor(doctNum,
                        startDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        endDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00");
            }
            if (result == null) {
                System.out.println("result set is empty!");
                return;
            }

            try {
                listReg.clear();
                while (result.next()) {
                    listReg.add(new Register(result.getString(Config.NameTableColumnRegisterNumber),
                            result.getString(Config.NameTableColumnPatientName),
                            result.getTimestamp(Config.NameTableColumnRegisterDateTime),
                            result.getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist)));
                }
                table_reg.setItems(listReg);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        else if (tabPane.getSelectionModel().getSelectedItem() == tabIncome) {
            ResultSet result = null;
            if(checkBox_allTime.isSelected()) {
                result = DBConnector.getInstance().getIncomeInfo("0000-00-00 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            else if (checkBox_today.isSelected()) {
                result = DBConnector.getInstance().getIncomeInfo(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            else {
                result = DBConnector.getInstance().getIncomeInfo(startDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        endDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00");
            }
            if (result == null) {
                System.out.println("result set is empty!");
                return;
            }
            try {
                listIncome.clear();
                while (result.next()) {
                    listIncome.add(new Income(result.getString("depname"),
                            result.getString(Config.NameTableColumnDoctorNumber),
                            result.getString("docname"),
                            result.getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist),
                            result.getInt(Config.NameTableColumnRegisterCurrentRegisterCount),
                            result.getDouble("sum")));
                }
                table_income.setItems(listIncome);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
