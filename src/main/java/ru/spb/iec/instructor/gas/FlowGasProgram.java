package ru.spb.iec.instructor.gas;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FlowGasProgram extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Pane root = loader.load();
        FlowGas controller =loader.getController();
        controller.setMainStage(stage);
        stage.setTitle("Расчет термодинамического цикла ГТД");
        if (stage.getScene() == null) {
            Scene scene = new Scene(root/* , 400, 400 */);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
        // stage.getIcons().add(new
        // Image(getClass().getResourceAsStream("/pictures/marketing8.png")));
        stage.centerOnScreen();
        stage.setFullScreen(true);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
