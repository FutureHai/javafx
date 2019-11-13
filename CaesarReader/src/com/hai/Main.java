package com.hai;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.Objects;

public class Main extends Application {
    private static final String favicon = "image/favicon.png";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        primaryStage.setTitle("自动阅读器");
        primaryStage.setScene(new Scene(loader.load(), 800, 600));
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream(favicon)));

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream(favicon)), "CR自动阅读器");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("CR自动阅读器");
        tray.add(trayIcon);

        primaryStage.setOnCloseRequest(event -> {
            Controller controller = loader.getController();

            ChromeDriver chromeDriver = controller.getDriver();
            if (Objects.nonNull(chromeDriver)) {
                chromeDriver.quit();
            }

            ChromeDriverService service = Controller.getService();
            if (Objects.nonNull(service)) {
                service.stop();
            }

            System.exit(0);
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
