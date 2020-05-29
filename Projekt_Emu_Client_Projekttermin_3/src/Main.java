import java.io.IOException;

import gui.BasisControl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) throws IOException{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BasisView.fxml"));
		Parent root = (Parent) loader.load();
		BasisControl controller = (BasisControl) loader.getController();
		controller.setStageAndSetupListeners(primaryStage, root); 
    }	
	
	public static void main(String... args){
		launch(args);
	}
}
