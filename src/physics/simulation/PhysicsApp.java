package physics.simulation;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import main.gui.AppSession;
import main.objects.Poly;
import main.objects.Node;
import main.objects.Camera;


public class PhysicsApp extends BorderPane implements AppSession {
	
	private Canvas canvas;
	private Point3D ViewFrom = new Point3D(20, 20, 30);
	private Point3D ViewTo = new Point3D(0, 0, 0);
	
	private Camera camera = new Camera(ViewTo, ViewFrom);
	
	private static final int OBJECT_SEED = 650;	
	
	private List<Poly> allPolygons = new ArrayList<Poly>();
	private Galaxy allObjects = new Galaxy(OBJECT_SEED);
	private Node aFrame = new Node(60, 60, 0, 9999);

	
	private BooleanProperty hasTransits = new SimpleBooleanProperty(false);
	private boolean renderPathing = false;
	private boolean pausePhysics = false;
	private boolean hasPathingContext = true;
	
	boolean isActiveSimulation = true;
	boolean fullFramePause = false;
	boolean showingResults = false;
	
	HBox gui;
	Scene scene;
	
	public PhysicsApp(Scene scene) {	
		setCanvas(scene);
		setControls(scene);
		renderCanvas();
	}
	
	private void setCanvas(Scene scene) {
		this.scene = scene;
		canvas = new Canvas();

		AnchorPane anchor = anchorCanvas(canvas);

	    setCenter(anchor);
		canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent e) {
				for (int i=allPolygons.size()-1; i>=0; i--) {
					if (allPolygons.get(i).contains(e.getX(), e.getY())) {
						// select object
						allObjects.setActive(allPolygons.get(i).getShapeNumber());
						break;
					}
				}
		    }
		});
	}
	
	private void setControls(Scene scene) {
		gui = new ControlPane(scene, this);
		hasTransits.addListener((observable, oldBool, newBool) -> {
				((ControlPane) gui).setFeature("Path Context", !newBool);
		    	renderPathing = !newBool ? false : renderPathing;
		});
		setLeft(gui);
	}
	
	public void renderCanvas() {
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		// set up viewing angle
		camera.setPerspective2D(scene);
		
		// update all objects
		allObjects.updateAllObjects(pausePhysics);
			
		// collect polygons to rendering list
		allPolygons.clear();
		allPolygons.addAll(allObjects.getPolygons(camera));

		// count number of physics-enabled polygons rendered
		camera.setPolygonsInView(allPolygons.size());
		
		// clear canvas and draw background
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, getWidth(), getHeight());

		// render grid polygons
		if (aFrame.isRenderable()) {
			gc.setLineWidth(1);
			gc.setStroke(Color.web("#1F1F1F"));
			for (Poly p : aFrame.getPolygons()) {
				p.updatePolygon(camera);
				gc.strokePolygon(p.getXP(), p.getYP(), 4);
			}
		}		
		
		// render dijkstra transits if enabled
		if (hasTransits.get()) {
			allObjects.drawAllTransits(gc, camera, hasPathingContext);
		}

		// draw all polygons
		if (hasPathingContext) {
			allPolygons.stream().forEach(p -> p.drawPolygon(gc));	
		}
		
		// if there is a primary selection, draw it
		allObjects.drawActive(camera, gc);

		
		// draw interface
		((ControlPane) gui).refreshInfo(camera, allObjects);
				
		// set timer to re-run this method
		if (isActiveSimulation) {

	        int milliSec = pausePhysics ? 150 : 15;
	        if (fullFramePause) {
		        milliSec = 3000;
		        fullFramePause = false;
	        } 
	        if (!fullFramePause && showingResults) {
	        	fullFramePause = false;
				pausePhysics = false;
				showingResults = false;
	        }
	        
		    new Timeline(new KeyFrame(Duration.millis(milliSec), ae -> renderCanvas())).play();	
		}
	}


	@Override
	public void killSession() {isActiveSimulation = false;}

	public Galaxy getGalaxy() {return allObjects;}
	
	public Camera getCamera() {return camera;}
	
	public void toggleGrid() {aFrame.toggleRenderable();}
	
	public void toggleDijkstra() {hasTransits.set(!hasTransits.get());}
	
	public void togglePathingContext() {hasPathingContext = !hasPathingContext;}
	
	public void viewFromTop() {camera = new Camera(ViewTo, ViewFrom = new Point3D(0, 1, 20));}
	
	public void viewFromFront() {camera = new Camera(ViewTo, ViewFrom = new Point3D(20, 0, 0));}

	
	public void pausePhysics() {
		pausePhysics = !pausePhysics; 
	}
	
	public void resetActive() {
		allObjects.resetActive(); 
	}
	

}