package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.sql.SQLException;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            //连接数据库
            DBConnector.getInstance().ConnectDataBase("localhost", 3306, "java_lab2", "java", "javajava");


            Parent root = FXMLLoader.load(getClass()
                    .getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setTitle("欢迎登录");
            //监听窗口关闭事件
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("close");
                    try {
                        DBConnector.getInstance().disConnectDataBase();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("关闭数据库时发生错误...");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("连接数据库发生错误...");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
