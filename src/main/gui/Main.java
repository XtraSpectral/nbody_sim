package main.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import main.simulation.PhysicsApp;


/**
 * @author Grant Turner
 * First Java and GUI project. Currently rebuilding in Python with no
 * intention to fix or clean in this version.
 * 
 * The 3D rendering functions are strongly influenced by JavaTutorials101 3D 
 * from Scratch tutorial: https://www.youtube.com/channel/UCvsTsOYDzwv0dMI7tfBFj6w
 * Traces of this code are found in the Vector, Poly, and Galaxy classes.
 */

public class Main extends Application {
	
	/**Application loop for javafx interface. Constructs the physics
	 * application and places it on the GUI with styles and sizing.
	 */
		
	private BorderPane mainWidget = new BorderPane(); // application container
	private Scene scene = new Scene(mainWidget, 1400, 1200);		
    private Stage stage;	
    
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage newStage) {   

		// initialize gradient
		Gradient.resetSpectra();
		
		// build new physics application, place in interface
		Pane newSimulation = new PhysicsApp(scene);  
		mainWidget.setCenter(newSimulation);
		
        // bind app layout to available space
		mainWidget.prefHeightProperty().bind(scene.heightProperty());
		mainWidget.prefWidthProperty().bind(scene.widthProperty());
		
        // add css to application pane
		mainWidget.getStylesheets().add("gui.css");
	    
		// set the stage
		stage = newStage;
		stage.setMinWidth(440);
		stage.setMinHeight(600);
		stage.setMaxWidth(3440);
		stage.setMaxHeight(1440);
		stage.setTitle("");
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> {endSession();});
		stage.show();
	}
	
	private void endSession() {
		// end mainwidget child processes
		mainWidget = null;
	}

}