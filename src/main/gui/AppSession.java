package main.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

public interface AppSession {
			
	// stops rendering this simulation
	public void killSession();
	
	// rendering process for this simulation
	public void renderCanvas();
	
	// anchors a canvas within a pane
	// resultant anchorpane will help canvas fill a boundary
	public default AnchorPane anchorCanvas(Canvas canvas) {
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

}
