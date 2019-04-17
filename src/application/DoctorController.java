package application;

import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    public static final class Register {
        private SimpleStringProperty number;
        private SimpleStringProperty patientName;
        private SimpleStringProperty regTime;
        private SimpleStringProperty regType;

        Register(String number, String patientName, String regTime, String regType) {
            this.number = new SimpleStringProperty(number);
            this.patientName = new SimpleStringProperty(patientName);
            this.regTime = new SimpleStringProperty(regTime);
            this.regType = new SimpleStringProperty(regType);
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
                      String regType, String regCount, String totalIncome) {
            this.deptName = new SimpleStringProperty(deptName);
            this.doctId = new SimpleStringProperty(doctId);
            this.doctName = new SimpleStringProperty(doctName);
            this.regType = new SimpleStringProperty(regType);
            this.regCount = new SimpleStringProperty(regCount);
            this.totalIncome = new SimpleStringProperty(totalIncome);
        }
    }
    public static String name;
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
            ResultSet result;

        }
    }
}
