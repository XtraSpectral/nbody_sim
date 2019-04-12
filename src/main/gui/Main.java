package main.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import physics.simulation.PhysicsApp;


public class Main extends Application {
		
	public Pane simulation = null;
	private BorderPane rootPane = new BorderPane(); //primary container
    public static final int topMenuHeight = 34;
	private Scene scene = new Scene(rootPane, 1400, 1200);		
    private Stage stage;	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage stage) {   

		this.stage = stage;
		
		// initialize gradient
		Gradient.generateGradient();
		
        // bind to take available space
		rootPane.prefHeightProperty().bind(scene.heightProperty());
		rootPane.prefWidthProperty().bind(scene.widthProperty());
		
        // add views to pane
		rootPane.getStylesheets().add("gui.css");
		this.newSession("Physics");
	    
		stage.setMinWidth(440);
		stage.setMinHeight(600);

		stage.setMaxWidth(3440);
		stage.setMaxHeight(1440);
		stage.setTitle("");
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> {endSession();});
		stage.show();
	}
	
	@SuppressWarnings("unused")
	public void newSession(String sessionType) {
		Pane newSimulation = null;
		
		// create a new simulation, return if the requested session type

		newSimulation = new PhysicsApp(scene);  
		
		if (newSimulation == null) {
			System.out.println("Main.newSession requested simulation is null");
			return;
		}
				
		replaceSession(newSimulation);
	}
	
	
	private void replaceSession(Pane newSim) {
		
		// close existing session
		endSession();
		
		// set new view
		simulation = newSim;
		rootPane.setCenter(simulation);
	}
	
	private void endSession() {
		// if the current view exists and extends AppSession, kill it
		// this prevents simulations from running after they are no longer in use
		if (simulation != null && simulation instanceof AppSession) {
			((AppSession) simulation).killSession();
		}	
	}
}