package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("App.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Treningsdagbok");
        stage.getIcons().add(new Image("http://i68.tinypic.com/v5kac1.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
	
	public static void main(String[] args) {
        launch(args);
    }
}
