package application;

import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {
    public static String name;
    @FXML
    private Label labelInfo;
    @FXML
    private Button buttonExit;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXRobotHelper.getStages().get(0).setTitle("");
        labelInfo.setText(name);
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
    }
}
