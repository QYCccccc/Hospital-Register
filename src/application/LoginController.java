package application;

import com.mysql.cj.protocol.ResultListener;
import com.sun.deploy.util.FXLoader;
import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


public class LoginController implements Initializable {
    @FXML
    TextField user, pass;
    @FXML
    Button Plogin, Dlogin;
    @FXML
    Label labelStatus;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //初始化函数，注册登录按钮事件处理
        Dlogin.setOnKeyReleased(KeyEvent -> {
            try {
                if (KeyEvent.getCode() == KeyCode.ENTER)
                    DoctorLogin();
            } catch (IOException e) {

            }
        });

        Plogin.setOnKeyReleased(KeyEvent -> {
            try {
                if (KeyEvent.getCode() == KeyCode.ENTER)
                    PatientLogin();
            } catch (IOException e) {

            }
        });
    }

    public void PatientLogin() throws IOException {
        if (!InputValid())
            return;
        //根据user id在数据库中查询
        ResultSet result = DBConnector.getInstance().getPatientInfo(user.getText().trim());
        if(result == null) {
            labelStatus.setText("读取数据库错误，请联系管理员...");
            labelStatus.setStyle("-fx-text-fill: red;");
        }
        try {
            if (!result.next()) {
                labelStatus.setText("用户不存在...");
                user.setStyle("-fx-border-color: red; ");
                labelStatus.setStyle("-fx-text-fill: red;");
                return;
            }
            else if (!result.getString(Config.NameTableColumnPatientPassword).equals(pass.getText())) {
                labelStatus.setText("密码错误！");
                labelStatus.setStyle("-fx-text-fill: red;");
                pass.setStyle("-fx-border-color: red; ");
                return ;
            }
            String pname = result.getString(Config.NameTableColumnPatientName);
            double pbalance = result.getDouble(Config.NameTableColumnPatientBalance);
            String pnum = result.getString(Config.NameTableColumnPatientNumber);
            PatientController.PatientName = pname;
            PatientController.PatientBalance = pbalance;
            PatientController.PatientNumber = pnum;
            DBConnector.getInstance().updatePatientLoginTime(pnum,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println(pnum + pname + "login\n" + pbalance);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/Patient.fxml")));
        FXRobotHelper.getStages().get(0).setScene(scene);
    }

    private boolean InputValid() {
        if (user.getText().isEmpty()) {
            user.setStyle("-fx-background-color: pink;");
            labelStatus.setText("登录失败...");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }
        if (pass.getText().isEmpty()) {
            pass.setStyle("-fx-background-color: pink;");
            labelStatus.setText("登录失败...");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }
        labelStatus.setText("登录中...");
        labelStatus.setStyle("");
        return true;
    }

    public void DoctorLogin() throws IOException {
        if (!InputValid())
            return;
        //根据doctor id查询数据库
        ResultSet resultSet = DBConnector.getInstance().getDoctorInfo(user.getText().trim());
        if (resultSet == null) {
            labelStatus.setText("读取数据库错误，请联系管理员...");
            labelStatus.setStyle("-fx-text-fill: red;");
        }
        try {
            if(!resultSet.next()) {
                labelStatus.setText("用户不存在...");
                user.setStyle("-fx-border-color: red; ");
                labelStatus.setStyle("-fx-text-fill: red;");
                return;
            }
            else if (!resultSet.getString(Config.NameTableColumnDoctorPassword).equals(pass.getText())) {
                labelStatus.setText("密码错误！");
                labelStatus.setStyle("-fx-text-fill: red;");
                pass.setStyle("-fx-border-color: red; ");
                return ;
            }
            System.out.println("Dlogin");
            String docName = resultSet.getString(Config.NameTableColumnDoctorName);
            DoctorController.name = docName;
            String docNum = resultSet.getString(Config.NameTableColumnDoctorNumber);
            System.out.println(docNum + docName);
            DBConnector.getInstance().updateDoctorLoginTime(docNum,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //加载医生登录界面
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/Doctor.fxml")));
        FXRobotHelper.getStages().get(0).setScene(scene);
    }
    @FXML
    public void onInputUserAction() {
        user.setStyle("");
    }
    @FXML
    public void onInputPassAction() {
        pass.setStyle("");
    }


}
