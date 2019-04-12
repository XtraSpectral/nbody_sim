package physics.simulation;

import java.util.Calendar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.gui.Gradient;
import main.laws.Physics;
import main.objects.Camera;
import main.objects.PhysicsNode;

public class ControlPane extends HBox {
	
	VBox controls = new VBox();
	InfoPane info = new InfoPane();

	public ControlPane(Scene scene, PhysicsApp render) {
		
		// configure control panel visuals
		controls.setPrefWidth(220);
		controls.setAlignment(Pos.CENTER);
		controls.setSpacing(5);
		controls.maxHeight(Double.MAX_VALUE);
		controls.getStylesheets().add("gui.css");
		controls.getStyleClass().add("controls");
		VBox.setVgrow(controls, Priority.ALWAYS);
		
		// create color bar and add to this with controls
		Canvas colorBar = makeColorBar(scene);
		getChildren().addAll(controls, colorBar);
		
		// add keyboard controls
		addKeyboardControls(scene, render);
		
		// add controls associated with the scene 
		addSceneControls(scene);
		
		// add controls related to the render and object container class
		addRenderControls(render);
		
		// add controls related to algorithms and data structures
		addAlgorithmControls(render);
		
		// add controls associated with the camera
		addCameraControls(render);
		
		// add last - compass, simulation, and selection information
		addControls(info);
	}
	
	
	
	/* ============================= */
	/* ===== KEYBOARD CONTROLS ===== */
	/* ============================= */
	
	public void addKeyboardControls(Scene scene, PhysicsApp render) {
		scene.setOnKeyPressed(e -> {
        	switch (e.getCode()) {
        		case LEFT: render.getCamera().moveOnAxis(new Point3D(-1, 0, 0)); break;
        		case RIGHT: render.getCamera().moveOnAxis(new Point3D(1, 0, 0)); break;
        		case UP: 
        			if (e.isControlDown()) {
        				render.getCamera().moveOnAxis(new Point3D(0, 0, 1));
        			} else {
        				render.getCamera().moveOnAxis(new Point3D(0, 1, 0)); 
        			}
        			break;
        		case DOWN: 
        			if (e.isControlDown()) {
        				render.getCamera().moveOnAxis(new Point3D(0, 0, -1));
        			} else {
        				render.getCamera().moveOnAxis(new Point3D(0, -1, 0)); 
        			}  
        			break;
        		case W: render.getCamera().zoom(1.0); break;
        		case A: render.getCamera().pan(1.0); break;
        		case S: render.getCamera().zoom(-1.0); break;
        		case D: render.getCamera().pan(-1.0); break;
        		case H: render.getGalaxy().scaleVelocity(1.5); break;
        		case ESCAPE: System.exit(0);
        		default: break;
        	}
		});
	}
	
	
	
	/* ========================== */
	/* ===== SCENE CONTROLS ===== */
	/* ========================== */
	
	private void addSceneControls(Scene scene) {
		scene.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		        System.out.println("Width: " + newSceneWidth);
		    }
		});
		scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		        System.out.println("Height: " + newSceneHeight);
		        newColorBar(scene);
		    }
		});		
		
		
		Label simLabel = new Label("Simulation");
		Slider dopplerSlider = new Slider();
		dopplerSlider.setMin(-250); // can go to 255
		dopplerSlider.setMax(250);
		dopplerSlider.setValue(0);

		Label redShift = new Label("RS");
		Label blueShift = new Label("BS");
		
		Button gradient = new Button("Reset Spectra");
		Button nextGradient = new Button("->");
		Button lastGradient = new Button("<-");
		gradient.setOnAction(e -> {Physics.resetG();});
		nextGradient.setOnAction(e -> {Physics.scaleG(.2);});
		lastGradient.setOnAction(e -> {Physics.scaleG(-.2);});
		Label gradientSchema = new Label(Gradient.getSpectra());
		Label gradientWavelength = new Label(Gradient.getWavelength());
		gradient.setOnAction(e -> {Gradient.resetSchema();
			updateColorLabels(scene, gradientSchema, gradientWavelength);
			dopplerSlider.setValue(0);
		});
		nextGradient.setOnAction(e -> {Gradient.nextSchema(1);
			updateColorLabels(scene, gradientSchema, gradientWavelength);
			dopplerSlider.setValue(0);
		});
		lastGradient.setOnAction(e -> {Gradient.nextSchema(-1);
			updateColorLabels(scene, gradientSchema, gradientWavelength);
			dopplerSlider.setValue(0);
		});
		dopplerSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				Gradient.dopplerShift(newVal.intValue());
				updateColorLabels(scene, gradientSchema, gradientWavelength);
			}
		});
		addControls(simLabel, gradientSchema, gradientWavelength);
		addMultiControl(lastGradient, gradient, nextGradient);
		addMultiControl(redShift, dopplerSlider, blueShift);
		
		Button resetGravity = new Button("Reset Gravity");
		Button increaseGravity = new Button("+G");
		Button decreaseGravity = new Button("-G");
		resetGravity.setOnAction(e -> {Physics.resetG();});
		increaseGravity.setOnAction(e -> {Physics.scaleG(1.5);});
		decreaseGravity.setOnAction(e -> {Physics.scaleG(.5);});
		addMultiControl(decreaseGravity, resetGravity, increaseGravity);
	}
	
	private void updateColorLabels(Scene scene, Label name, Label desc) {
    	newColorBar(scene);
    	name.setText(Gradient.getSpectra());
    	desc.setText(Gradient.getWavelength());
	}

	
	
	
	/* =========================== */
	/* ===== RENDER CONTROLS ===== */
	/* =========================== */
	
	private void addRenderControls(PhysicsApp render) {

		Button increaseEnergy = new Button("+E");
		Button resetEnergy = new Button("Reset Energy");
		Button decreaseEnergy = new Button("-E");
		increaseEnergy.setOnAction(e -> {render.getGalaxy().scaleEnergy(1.1);});
		decreaseEnergy.setOnAction(e -> {render.getGalaxy().scaleEnergy(.9);});
		resetEnergy.setOnAction(e -> {render.getGalaxy().resetEnergy();});
		addMultiControl(decreaseEnergy, resetEnergy, increaseEnergy);
		
		Button ignite = new Button("Ignite to Energy Source");
		ignite.setOnAction(e -> {render.getGalaxy().makeStar();});
		addControls(ignite);
		
		Button contractSpace = new Button("Contract Space");
		Button expandSpace = new Button("Expand Space");
		contractSpace.setOnAction(e -> {render.getGalaxy().scaleSpace(.6);});
		expandSpace.setOnAction(e -> {render.getGalaxy().scaleSpace(1.1);});
		addMultiControl(contractSpace, expandSpace);

		Button halt = new Button("(H)alt Physics");
		halt.setOnAction(e -> {render.getGalaxy().scaleVelocity(0);});
		
		Button pause = new Button("Pause Physics");
		pause.setOnAction(e -> {render.pausePhysics();});
		
		Button toggleGrid = new Button("Toggle Grid");
		toggleGrid.setOnAction(e -> {render.toggleGrid();});
		
		Button deselect = new Button("Deselect All");
		deselect.setOnAction(e -> {render.resetActive();});

		Button reseed = new Button("Reset Seed");
		reseed.setOnAction(e -> {render.getGalaxy().reset();});
		
		addControls(halt, pause, toggleGrid, deselect, reseed);
		

	}
	
	
	
	/* ============================== */
	/* ===== ALGORITHM CONTROLS ===== */
	/* ============================== */
	
	private void addAlgorithmControls(PhysicsApp render) {
		
		Label algorithmLabel = new Label("Graphs and Algorithms");
		
		Button edges = new Button("Render Graph Edges");
		edges.setOnAction(e -> {render.toggleDijkstra();});
		
		Button pathContext = new Button("Toggle Dimensional Context");
		pathContext.setId("Path Context");
		pathContext.setDisable(true);
		pathContext.setOnAction(e -> {render.togglePathingContext();});
		
		addControls(algorithmLabel, edges, pathContext);

	}

	
	
	
	
	/* =========================== */
	/* ===== CAMERA CONTROLS ===== */
	/* =========================== */

	private void addCameraControls(PhysicsApp render) {
		
		Label cameraLabel = new Label("Camera");
		
		Button viewFromTop = new Button("View Top");
		Button viewFromFront = new Button("View Front");
		viewFromTop.setOnAction(e -> {render.viewFromTop();});
		viewFromFront.setOnAction(e -> {render.viewFromFront();});
		addMultiControl(viewFromTop, viewFromFront);

		Button moveAlong2D = new Button("Move on X and Y");
		moveAlong2D.setOnAction(e -> {render.getCamera().moveForwardWithFocus();});
		
		Button resetCameraPos = new Button("Reset Camera Position");
		resetCameraPos.setOnAction(e -> {render.getCamera().resetPosition();});
		
		addControls(cameraLabel, moveAlong2D, resetCameraPos);
	}
	
	/* ================================== */
	/* ===== CONTROL GUI MECHANISMS ===== */
	/* ================================== */
	
	public void newColorBar(Scene scene) {
		// replace the color bar at index 1 of this hbox
		getChildren().set(1, makeColorBar(scene));
	}

	private void addMultiControl(Node... elements) {
		// add new controls in the same box/line
		addControls(true, elements);
	}
	
	public void addControls(Node... elements) {
		// TODO make private when no more controls are being made in PhysicsRender
		addControls(false, elements);
	}
	
	public void addControls(boolean multiControl, Node... elements) {
		HBox multiBox = multiControl ? new HBox() : null;
		for (Node n : elements) {
			if (n instanceof Button) {
				((Button) n).setMaxWidth(controls.getPrefWidth());
				((Button) n).setMaxHeight(controls.getHeight());
				((Button) n).setMinHeight(26);
			} else if (n instanceof Label) {
				((Label) n).setPadding(new Insets(0, 0, 0, 5));
			}
			
			if (multiControl) {
				multiBox.getChildren().add(n);
			} else {
				controls.getChildren().add(n);
			}

			HBox.setHgrow(n, Priority.ALWAYS);
			VBox.setVgrow(n, Priority.ALWAYS);
		}
		if (multiControl) {
			multiBox.setSpacing(5);
			controls.getChildren().add(multiBox);
		}
	}

	private Canvas makeColorBar(Scene scene) {
		Canvas gradient = new Canvas();
		gradient.setWidth(5);
		gradient.heightProperty().bind(scene.heightProperty());

		GraphicsContext gr = gradient.getGraphicsContext2D();
		gr.setFill(Color.BLACK);
		gr.fillRect(0, 0, gradient.getWidth(), gradient.getHeight());
		
		Gradient.drawColorScale(gr);
		
		gradient.maxWidth(Double.MAX_VALUE);
		gradient.maxHeight(Double.MAX_VALUE);
		HBox.setHgrow(gradient, Priority.ALWAYS);
		VBox.setVgrow(gradient, Priority.ALWAYS);
		
		return gradient;
	}
	
	public void setFeature(String featureName, boolean isEnabled) {
		// look up the id of an item placed in the controls, then en/dis-able
		for (Node n : controls.getChildren()) {
			if (n.getId() != null && n.getId().equals(featureName)) {
				n.setDisable(isEnabled);
			}
		}
	}
	
	public void refreshInfo(Camera camera, Galaxy galaxy ) {
		info.updateInfoPane(camera, galaxy);
	}
}

class InfoPane extends VBox {
	
	private int fontSize = 14; // space between 
	private Font f = new Font("Consolas", fontSize);
	private int center = 105;
	
	Label currentTime = new Label();
	
	Label physicsG = new Label();
	Label physicsAU = new Label();
	Label physicsScale = new Label();
	Label physicsTimeScale = new Label();
	
	Label vRotation = new Label();
	Label vInclination = new Label();
	Label vDistance = new Label();
	Label vViewFrom = new Label();
	Label vViewTo = new Label();

	Canvas compass = new Canvas(center*2, center*2);
	
	Label pIdNumber = new Label();
	Label pName = new Label();
	Label pPosition = new Label();
	Label pVelocity = new Label();
	Label pAcceleration = new Label();
	Label pTemperature = new Label();
	
	
	public InfoPane() {
		getChildren().addAll(currentTime);
		getChildren().addAll(physicsG, physicsAU, physicsScale, physicsTimeScale);
		getChildren().addAll(vRotation, vInclination, vDistance, vViewFrom, vViewTo);
		getChildren().addAll(compass);
		getChildren().addAll(pIdNumber, pName, pPosition, pVelocity, pAcceleration, pTemperature);
	    setPadding(new Insets(0, 0, 0, 5));
	    setSpacing(2);
	    
	    physicsG.setText(String.format("G = %6.4e", Physics.G));
	    physicsAU.setText(String.format("AU = %6.2e km", Physics.AU));
	    physicsScale.setText(String.format("Scale = %6.2e km", Physics.scaleAU));
	    physicsTimeScale.setText(String.format("Time Scale = %.0f", Physics.timeScale));
	}
	
	public void updateInfoPane(Camera v, Galaxy g) {
		
	    physicsG.setText(String.format("G = %6.4e", Physics.G));
		
		
		// generate timestamp
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int mHour = calendar.get(Calendar.HOUR_OF_DAY);
		int mMinute = calendar.get(Calendar.MINUTE);
		int mSeconds = calendar.get(Calendar.SECOND);
		int mMilliseconds = calendar.get(Calendar.MILLISECOND);
		currentTime.setText(String.format("%s:%s:%s:%s", mHour, mMinute, mSeconds, mMilliseconds));
		
		// update individual sections of this pane
		updateVectorLabels(v);
		updateActiveObjectLabels(g.getActive());
		updateCompass(v, g.getActive());
	}
	
	private void updateCompass(Camera v, PhysicsNode p) {
		GraphicsContext gc = compass.getGraphicsContext2D();
		gc.setFont(f); 
		gc.setLineWidth(1);
		
		gc.clearRect(0, 0, compass.getWidth(), compass.getHeight());
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, compass.getWidth(), compass.getHeight());
		
		Point3D viewTo = v.getViewTo();
		
		Point2D pC = v.transformRelativePointTo2D(new Point3D(0,0,0));
		Point2D pX = v.transformRelativePointTo2D(new Point3D(2,0,0));
		Point2D pY = v.transformRelativePointTo2D(new Point3D(0,2,0));
		Point2D pZ = v.transformRelativePointTo2D(new Point3D(0,0,2));

		// draw compass lines and labels
		gc.setStroke(Color.BLUEVIOLET);
		Point2D xLine = pX.subtract(pC);
		Point2D yLine = pY.subtract(pC);
		Point2D zLine = pZ.subtract(pC);

		drawCompassLine(gc, "X+", (int) xLine.getX(), (int) xLine.getY()); 
		gc.setStroke(Color.MEDIUMVIOLETRED);
		drawCompassLine(gc, "Y+", (int) yLine.getX(), (int) yLine.getY());
		gc.setStroke(Color.DEEPSKYBLUE);
		drawCompassLine(gc, "Z+", (int) zLine.getX(), (int) zLine.getY());
		
		// draw compass line to active object
		if (p != null) {
			Point3D point3D = p.getXYZ();
			Point2D pP = v.transformRelativePointTo2D(new Point3D(point3D.getX(),point3D.getY(),point3D.getZ()));
			gc.setStroke(Color.GREEN);
			Point2D activeLine = pP.subtract(pC);
			drawCompassLine(gc, "", (int) activeLine.getX(), (int) activeLine.getY());
		}
		
		// draw center of mass pointer and labels
		String cL = String.format("%.1f %.1f %.1f", viewTo.getX(), viewTo.getY(), viewTo.getZ());
		gc.setStroke(Color.DARKORANGE);
		gc.setFill(Color.DARKORANGE);
		gc.strokeText(cL, 1, compass.getHeight()-1);
		gc.fillRect(center-1, center-1, 2, 2);
	}
	
	private void drawCompassLine(GraphicsContext gc, String label, int x, int y) {
		int dim1X = (Math.abs(x)<100 ? center+x : center+100*(y<0 ? -1 : 1));
		int dim1Y = (Math.abs(y)<100 ? center+y : center+100*(y<0 ? -1 : 1));
		gc.strokeLine(center, center, dim1X, dim1Y);
		gc.strokeText(label, (center+x>center+50 ? center+50 : center+x), 
			 	(center+y>center+50 ? center+50 : center+y));
	}
	
	private void updateVectorLabels(Camera v) {

		vRotation.setText(String.format("ROTATION: %.1f rads", v.getRotation()));
		vInclination.setText(String.format("INCLINATION: %.1f rads", v.getInclination()));
		vDistance.setText(String.format("DISTANCE: %.1f", v.getDistance()));
		Point3D viewTo = v.getViewTo();
		Point3D viewFrom = v.getViewFrom();
		vViewFrom.setText(String.format("CAMERA %.1f %.1f %.1f", viewFrom.getX(), viewFrom.getY(), viewFrom.getZ()));
		vViewTo.setText(String.format("FOCUS %.1f %.1f %.1f", viewTo.getX(), viewTo.getY(), viewTo.getZ()));
	}
	
	private void updateActiveObjectLabels(PhysicsNode p) {

		if (p != null) {
			pIdNumber.setText(String.format("ID: %-1s", p.getID()));
			pName.setText(String.format("NAME: %-1s", p.toString()));
			Point3D point3D = p.getXYZ();
			pPosition.setText(String.format("POS X: %.2f Y: %.2f Z: %.2f", point3D.getX(), point3D.getY(), point3D.getZ()));
			Point3D velocity = p.getXYZVelocity();
			pVelocity.setText(String.format("VEL X: %.2f Y: %.2f Z: %.2f", velocity.getX()*Physics.scaleAU/1000000, velocity.getY()*Physics.scaleAU/1000000, velocity.getZ()*Physics.scaleAU/1000000));
			Point3D acceleration = p.getXYZAcceleration();
			pAcceleration.setText(String.format("ACC X: %.2f Y: %.2f Z: %.2f", acceleration.getX()*Physics.scaleAU/1000000, acceleration.getY()*Physics.scaleAU/1000000, acceleration.getZ()*Physics.scaleAU/1000000));
			pTemperature.setText(String.format("TEMP: %dK", p.getKelvin()));
		} else {
			if (!pIdNumber.getText().equals("")) {
				pIdNumber.setText("");
				pName.setText("");
				pPosition.setText("");
				pVelocity.setText("");
				pAcceleration.setText("");
				pTemperature.setText("");
			}
		}
	}
	
}

