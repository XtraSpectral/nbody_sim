package main.simulation;

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
import javafx.util.Duration;
import main.objects.Poly;
import main.objects.Node;
import main.objects.Vector;


public class PhysicsApp extends BorderPane {
	
	/**Primary GUI viewing widget, but also containing the
	 * application's model (Galaxy) and controller (ControlPane).
	 */
	
	private static final int OBJECT_SEED = 650;	// count of objects to generate for simulation
	
	private List<Poly> allPolygons = new ArrayList<Poly>(); // polygons to draw in the current frame
	private Galaxy allObjects = new Galaxy(OBJECT_SEED); // data structure of simulation objects
	private Vector camera = Vector.defaultCamera(); // camera for drawing perspective
	private Node aFrame = new Node(60, 60, 0, 9999); // plane of reference that can be drawn

	private BooleanProperty hasTransits = new SimpleBooleanProperty(false); // enables drawing graph edges
	private boolean pausePhysics = false; // allows pausing the simulation, preventing object updated
	
	private Scene scene; // parent application scene
	private Canvas canvas; // vector graphics canvas
	private ControlPane gui; // interface pane containing simulation controls
	
	public PhysicsApp(Scene scene) {	
		this.scene = scene;
		setCanvas(scene);
		setControls(scene);
		renderCanvas();
	}
	
	private AnchorPane anchorCanvas(Canvas canvas) {
		AnchorPane anchor = new AnchorPane();
		anchor.getChildren().add(canvas);
		canvas.widthProperty().bind(anchor.widthProperty());
		canvas.heightProperty().bind(anchor.heightProperty());
		AnchorPane.setTopAnchor(canvas, 0.0);
		AnchorPane.setBottomAnchor(canvas, 0.0);
		AnchorPane.setLeftAnchor(canvas, 0.0);
		AnchorPane.setRightAnchor(canvas, 0.0);
		return anchor;
	}
	
	private void setCanvas(Scene scene) {
		canvas = new Canvas();
		AnchorPane anchor = anchorCanvas(canvas);
	    setCenter(anchor);
	    
	    // add a listener for mouseclicks - check which polygon was clicked, then select it
		canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent e) {
				for (int i=allPolygons.size()-1; i>=0; i--) {
					if (allPolygons.get(i).contains(e.getX(), e.getY())) {
						allObjects.setActive(allPolygons.get(i).getShapeNumber());
						break;
					}
				}
		    }
		});
	}
	
	private void setControls(Scene scene) {
		gui = new ControlPane(scene, this);
		setLeft(gui);
	}
	
	public void renderCanvas() {
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		// MUST BE FIRST CAMERA OPERATION - set up viewing angle
		camera.setPerspective2D(scene);
		
		// update all objects
		allObjects.updateAllObjects(pausePhysics);

		// clear canvas and draw background
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, getWidth(), getHeight());

		// render grid polygons, if grid is enabled
		if (aFrame.isRenderable()) {
			gc.setLineWidth(1);
			gc.setStroke(Color.web("#1F1F1F"));
			for (Poly p : aFrame.getPolygons()) {
				p.update(camera);
				gc.strokePolygon(p.getXP(), p.getYP(), 4);
			}
		}		

		// render transits, giving the appearance of a connected graph
		if (hasTransits.get()) {
			allObjects.drawAllTransits(gc, camera);
		}

		// collect polygons to rendering list
		allPolygons.clear();
		allPolygons.addAll(allObjects.getPolygons(camera));
		allPolygons.stream().forEach(p -> p.draw(gc));	
		
		// if there is a primary selection, draw it
		allObjects.drawActive(camera, gc);
		
		// update information panes in interface
		gui.refreshInfo(camera, allObjects);
				
		// set timer to re-run this method
		new Timeline(new KeyFrame(Duration.millis(15), ae -> renderCanvas())).play();	
	}

	
	public Galaxy getGalaxy() {return allObjects;}
	
	public Vector getCamera() {return camera;}
	
	// toggle the display of a plane along the x and y axis
	public void toggleGrid() {aFrame.toggleRenderable();}
	
	// toggle the display of transits as graph edges between nodes
	public void toggleTransits() {hasTransits.set(!hasTransits.get());}
	
	// move camera directly above and facing its current focus
	public void viewFromTop() {camera = new Vector(camera.getFoci(), new Point3D(0, 1, 20));}
	
	// move camera directly in front of and facing the current focus
	public void viewFromFront() {camera = new Vector(camera.getFoci(), new Point3D(20, 0, 0));}
		
	// pauses the application of forces in the simulation
	public void pausePhysics() {pausePhysics = !pausePhysics;}
	
	// deselect any selected objects
	public void resetActive() {allObjects.resetActive();}
	

}