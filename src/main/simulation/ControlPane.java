package main.simulation;

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
import main.objects.Vector;
import main.objects.PhysicsNode;

public class ControlPane extends HBox {
	
	/**GUI object containing all controls for simulation 
	 * and application. 
	 */
	
	VBox controls;
	
	private int center = 105;
	private Canvas compass = new Canvas(center*2, center*2);
	private int fontSize = 14; // space between 
	private Font f = new Font("Consolas", fontSize);

	private Label pIdNumber;
	private Label pPosition;
	private Label pVelocity;
	private Label pAcceleration;
	private Label pTemperature;
	private Label pMass;
	
	private Label vRotation;
	private Label vInclination;
	private Label vOrigin;
	private Label vFoci;

	public ControlPane(Scene scene, PhysicsApp render) {
		
		controls = makeControlPanel();
		Canvas colorBar = makeColorBar(scene);

		// create color bar and add to this with controls
		getChildren().addAll(controls, colorBar);
		
		// add keyboard controls
		addKeyboardControls(scene, render);
		
		// add GUI controls
		addSceneControls(scene, render);	
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
        		case W: render.getCamera().dolly(1.0); break;
        		case A: render.getCamera().pan(1.0); break;
        		case S: render.getCamera().dolly(-1.0); break;
        		case D: render.getCamera().pan(-1.0); break;
        		case E: render.getGalaxy().scaleVelocity(1.5); break;
        		case Q: render.getGalaxy().scaleVelocity(.67); break;
        		case R: render.getCamera().moveForwardWithFocus(); break;
        		case ESCAPE: System.exit(0);
        		default: break;
        	}
		});
	}
	
	
	/* ========================== */
	/* ===== WIDGET FACTORY ===== */
	/* ========================== */
	
	public VBox makeControlPanel() {
		VBox controls = new VBox();
		controls.setPrefWidth(220);
		controls.setSpacing(5);
		controls.maxHeight(Double.MAX_VALUE);
		controls.getStyleClass().add("controls");
		VBox.setVgrow(controls, Priority.ALWAYS);
		return controls;
	}
	
	public Label makeLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		return label;
	}
	
	
	private Label makeSectionLabel(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("sectionlabel");
		label.setMaxWidth(Double.MAX_VALUE);
		return label;
	}
	
	private Label makeUnitLabel(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("unitlabel");
		label.setMaxWidth(Double.MAX_VALUE);
		label.setAlignment(Pos.CENTER_RIGHT);
		return label;
	}	

	private static Slider makeSlider() {
		Slider slider = new Slider();
		slider.setMin(-6); // can go to 255
		slider.setMax(6);
		slider.setValue(0);
		slider.setBlockIncrement(1);
		slider.showTickMarksProperty().set(true);
		slider.setSnapToTicks(true);
		slider.setMajorTickUnit(6);
		slider.setMinorTickCount(5);
		return slider;
	}
	

	/* ========================== */
	/* ===== SCENE CONTROLS ===== */
	/* ========================== */
	
	private void addSceneControls(Scene scene, PhysicsApp render) {
		scene.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		        // System.out.println("Width: " + newSceneWidth);
		    }
		});
		scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//		        System.out.println("Height: " + newSceneHeight);
		        newColorBar(scene);
		    }
		});	
		
		
		
		addControls(compass);

		
		
		Slider dopplerSlider = makeSlider();
		Label dopplerLabel = makeSectionLabel("Doppler Shift");
		Label dopplerUnits = makeUnitLabel("+0%");
		
		Label spectraLabel = makeSectionLabel("Spectra");
		Label gradientSchema = makeUnitLabel(Gradient.getSpectra());
		Label gradientWavelength = makeUnitLabel(Gradient.getWavelength());

		dopplerSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				Gradient.dopplerShift(newVal.intValue());
				updateColorLabels(scene, gradientSchema, gradientWavelength);
				int adjust = newVal.intValue() * 5;
				dopplerUnits.setText(String.format("%s%d%%", adjust < 0 ? "-" : "+", Math.abs(adjust)));
				
			}
		});
		
		
		addMultiControl(spectraLabel, gradientSchema);
		Slider spectraSlider = makeSlider();
		spectraSlider.setOnDragDone(e -> {});
		spectraSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				Gradient.selectSchema(newVal.intValue() + 6);
				updateColorLabels(scene, gradientSchema, gradientWavelength);
			}
		});
		spectraSlider.setValue(-6);
		addControls(spectraSlider);

		addMultiControl(dopplerLabel, dopplerUnits);
		addControls(dopplerSlider);
		
		
		
		
		
				
		Label gravityLabel = makeSectionLabel("Gravity");
		Label gravityUnits = makeUnitLabel(String.format("%6.1e mkg/s", Physics.G));
		Slider gravitySlider = makeSlider();
		gravitySlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				// normalize then make scalar as 0 <= adjust <= 2
				double adjust = ((double) newVal / 6) + 1;
				// increasing scales should scale by an exponential factor
				adjust = adjust >= 1 ? Math.pow(adjust, 5) : adjust;
				double newG = Physics.scaleG(adjust);
				gravityUnits.setText(String.format("%6.1e mkg/s", newG));
			}
		});
		addMultiControl(gravityLabel, gravityUnits);
		addControls(gravitySlider);



		
		
		
		Label energyLabel = makeSectionLabel("Energy");
		Label energyUnits = makeUnitLabel("x1.00");
		addMultiControl(energyLabel, energyUnits);
		Slider energySlider = makeSlider();
		energySlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				// normalize then make scalar as 0 <= adjust <= 2
				double adjust = ( newVal.doubleValue() / 6) + 1;
				// increasing scales should scale by an exponential factor
				render.getGalaxy().scaleEnergy(adjust);
				energyUnits.setText(String.format("x%.2f", adjust));
			}
		});
		addControls(energySlider);
		
		Button ignite = new Button("Ignite Object");
		ignite.setOnAction(e -> {render.getGalaxy().makeStar();});
		addControls(ignite);
		
		
		
		
		
		
		Label spaceLabel = makeSectionLabel("Space");
		Label spaceUnits = makeUnitLabel("0");
		
		addMultiControl(spaceLabel, spaceUnits);
		
		Slider spaceSlider = makeSlider();
		spaceSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isChanging) {
		        if (!isChanging) {
		        	double value = spaceSlider.getValue();
		        	double scale = 1 + (value / 10);
					render.getGalaxy().scaleSpace(scale);

		        	spaceSlider.setValue(0);
		        	spaceUnits.setText("-");
		        }
		    }
		});
		spaceSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				if (newVal.intValue() == 0) {
					spaceUnits.setText("-");
				} else {
					String unitText = String.format("%d%%", newVal.intValue() * 10);
		        	spaceUnits.setText(unitText);
				}
			}
		});
		addControls(spaceSlider);

		Button halt = new Button("Halt Physics");
		Button pause = new Button("Pause Physics");
		halt.setOnAction(e -> {render.getGalaxy().scaleVelocity(0);});
		pause.setOnAction(e -> {render.pausePhysics();});
		addControls(halt, pause);
		
		
		
		Label geometryLabel = makeSectionLabel("Geometry");
		addControls(geometryLabel);
		
		Button toggleGrid = new Button("Toggle Grid");
		toggleGrid.setOnAction(e -> {render.toggleGrid();});	
		
		Button edges = new Button("Render Graph Edges");
		edges.setOnAction(e -> {render.toggleTransits();});
			
		addControls(toggleGrid, edges);
		
		
		
		
		
		
		
		
		
		
		
		
		
		Label cameraLabel = makeSectionLabel("Camera");
		
		Button viewFromTop = new Button("View Top");
		Button viewFromFront = new Button("View Front");
		viewFromTop.setOnAction(e -> {render.viewFromTop();});
		viewFromFront.setOnAction(e -> {render.viewFromFront();});
		
		Button resetCameraPos = new Button("Reset Camera Position");
		resetCameraPos.setOnAction(e -> {render.getCamera().resetPosition();});
		
		
		
		Label vRotationLabel = makeLabel("Rotation (rads)");
		Label vInclinationLabel = makeLabel("Inclination (rads)");
		Label vOriginLabel = makeLabel("Position");
		Label vFociLabel = makeLabel("Focus");
		vRotation = makeUnitLabel("");
		vInclination = makeUnitLabel("");
		vOrigin = makeUnitLabel("");
		vFoci = makeUnitLabel("");

		
		
		
		addControls(cameraLabel);
		
		addMultiControl(vRotationLabel, vRotation);
		addMultiControl(vInclinationLabel, vInclination);
		addMultiControl(vOriginLabel, vOrigin);
		addMultiControl(vFociLabel, vFoci);
		addControls(resetCameraPos);
		addControls(viewFromTop, viewFromFront);

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		Label resetLabel = makeSectionLabel("Reset");
		addControls(resetLabel);
		
		Button deselect = new Button("Reset Selection");
		deselect.setOnAction(e -> {render.resetActive();});

		Button reseed = new Button("Reset Seed");
		reseed.setOnAction(e -> {
			render.getGalaxy().reset();
		});
		addControls(deselect, reseed);
		
		
		
		
			
		pIdNumber = makeSectionLabel("Object");
		Label pPositionLabel = makeLabel("Position");
		Label pVelocityLabel = makeLabel("Velocity");
		Label pAccelerationLabel = makeLabel("Accel.");
		Label pTemperatureLabel = makeLabel("Temperature (K)");
		Label pMassLabel = makeLabel("Mass (kg)");
		pPosition = makeUnitLabel("");
		pVelocity = makeUnitLabel("");
		pAcceleration = makeUnitLabel("");
		pTemperature = makeUnitLabel("");
		pMass = makeUnitLabel("");

		addControls(pIdNumber);
		addMultiControl(pPositionLabel, pPosition);
		addMultiControl(pVelocityLabel, pVelocity);
		addMultiControl(pAccelerationLabel, pAcceleration);
		addMultiControl(pTemperatureLabel, pTemperature);
		addMultiControl(pMassLabel, pMass);
	}

	
	/* ================================== */
	/* ===== CONTROL GUI MECHANISMS ===== */
	/* ================================== */
	
	private void updateColorLabels(Scene scene, Label name, Label desc) {
    	newColorBar(scene);
    	name.setText(Gradient.getSpectra());
    	desc.setText(Gradient.getWavelength());
	}

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
				((Button) n).setMaxHeight(26);
			} else if (n instanceof Label) {
				((Label) n).setPadding(new Insets(0, 5, 0, 5));
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
		Canvas canvas = new Canvas();
		canvas.setWidth(5);
		canvas.heightProperty().bind(scene.heightProperty());

		GraphicsContext gr = canvas.getGraphicsContext2D();
		gr.setFill(Color.BLACK);
		gr.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		Gradient.drawColorScale(gr);
		
		canvas.maxWidth(Double.MAX_VALUE);
		canvas.maxHeight(Double.MAX_VALUE);
		HBox.setHgrow(canvas, Priority.ALWAYS);
		VBox.setVgrow(canvas, Priority.ALWAYS);
		
		return canvas;
	}
	
	public void refreshInfo(Vector camera, Galaxy galaxy ) {
		updateCompass(camera, galaxy.getActive());
		updateVectorLabels(camera);
		updateActiveObjectLabels(galaxy.getActive());
	}
	
	private void updateCompass(Vector v, PhysicsNode p) {
		GraphicsContext gc = compass.getGraphicsContext2D();
		gc.setFont(f); 
		gc.setLineWidth(1);
		
		gc.clearRect(0, 0, compass.getWidth(), compass.getHeight());
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, compass.getWidth(), compass.getHeight());
			
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
		gc.fillRect(center-1, center-1, 2, 2);
	}
	
	private void drawCompassLine(GraphicsContext gc, String label, int x, int y) {
		int dim1X = (Math.abs(x)<100 ? center+x : center+100*(y<0 ? -1 : 1));
		int dim1Y = (Math.abs(y)<100 ? center+y : center+100*(y<0 ? -1 : 1));
		gc.strokeLine(center, center, dim1X, dim1Y);
		gc.strokeText(label, (center+x>center+50 ? center+50 : center+x), 
			 	(center+y>center+50 ? center+50 : center+y));
	}
	
	
	private void updateVectorLabels(Vector v) {

		vRotation.setText(String.format("%.1f", v.getRotation()));
		vInclination.setText(String.format("%.1f", v.getInclination()));
		Point3D foci = v.getFoci();
		Point3D origin = v.getOrigin();
		vOrigin.setText(String.format("%.1f %.1f %.1f", origin.getX(), origin.getY(), origin.getZ()));
		vFoci.setText(String.format("%.1f %.1f %.1f", foci.getX(), foci.getY(), foci.getZ()));
	}
	
	
	private void updateActiveObjectLabels(PhysicsNode p) {

		if (p != null) {
			pIdNumber.setText(String.format("Object %-1s", p.getID()));
			Point3D point3D = p.getXYZ();
			pPosition.setText(String.format("%.1f, %.1f, %.1f", point3D.getX(), point3D.getY(), point3D.getZ()));
			Point3D velocity = p.getXYZVelocity();
			pVelocity.setText(String.format("%.1f, %.1f, %.1f", velocity.getX()*Physics.scaleAU/1000000, velocity.getY()*Physics.scaleAU/1000000, velocity.getZ()*Physics.scaleAU/1000000));
			Point3D acceleration = p.getXYZAcceleration();
			pAcceleration.setText(String.format("%.1f, %.1f, %.1f", acceleration.getX()*Physics.scaleAU/1000000, acceleration.getY()*Physics.scaleAU/1000000, acceleration.getZ()*Physics.scaleAU/1000000));
			pTemperature.setText(String.format("%d", p.getKelvin()));
			pMass.setText(String.format("%e", p.getMass()/1000.0/1000.0));
		} else {
			if (!pIdNumber.getText().equals("")) {
				pIdNumber.setText("Object");
				pPosition.setText("");
				pVelocity.setText("");
				pAcceleration.setText("");
				pTemperature.setText("");
				pMass.setText("");
			}
		}
	}
}

