package main.objects;


import javafx.scene.paint.Color;
import java.awt.Polygon;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;

public class Poly extends Polygon {
	
	private static final long serialVersionUID = 1L;
	private Color c; // color of the drawable graphic's body
	private Color f; // color of polygon frame
	private double[] x, y, z; // default coordinates for each point
	private double avgDist = 0.0; // render distance from camera
	private boolean selected = false; // selection by mouse
	private int shapeNumber;
	private boolean isTestInstance = false;
	private boolean drawFrameOnly = false;
	
	// need in double format for javaFX canvas strokePolygon() and fillPolygon()
	private double[] xp;
	private double[] yp;
	
	@Override
	public String toString() {
		return String.valueOf(shapeNumber);
	}
	
	public double[] getXP() {
		return xp;
	}
	
	public double[] getYP() {
		return yp;
	}
	
	public Poly(double[] x, double[] y, double[] z, int i) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.shapeNumber = i;
	
		this.f = Color.BLACK;
 		// add perspective to 2d face for 3d appearance
	}

	
	public void updatePolygon(Camera viewVector) {
		this.reset();
		
		xp = new double[4];
		yp = new double[4];
		
		for (int i=0; i<4; i++) {
			Point2D n = viewVector.transformPointTo2D(new Point3D(x[i], y[i], z[i]));
			addPoint((int) n.getX(), (int) n.getY()); // legacy java swing polygon points
			xp[i] = n.getX();
			yp[i] = n.getY();
			
		}

		
//		IntStream.range(0, x.length)
//				 .mapToObj(i -> viewVector.transformPointTo2D(x[i], y[i], z[i]))
//				 .forEach(i -> addPoint((int) i[0], (int) i[1]));	
		
		this.avgDist = viewVector.getAvgDistanceFromOrigin(x, y, z);
		
	}

	public void drawPolygon(GraphicsContext g) {
		drawPolygon(g, c);
	}
	
	public void drawPolygon(GraphicsContext g, Color c) {
		
//		if (!drawFrameOnly) {
//			// draw shape fill, can set different color when selected here
//			g.setFill(Color.BLACK);
//			g.fillPolygon(this.getX(), this.getY(), 4);
//		}
		g.setFill(c);
		
		
				
		
		g.fillPolygon(xp, yp, 4);
		g.fill();
        g.stroke();
		// draw shape outline, chooing different color if selected
//		g.setColor((selected ? Color.green : Color.black));
//		if (drawFrameOnly) {g.setColor(Color.darkGray);}
//		g.drawPolygon(this);
		
		// draw selection details
		if (selected) {
			
			// red pixels on corner
			g.setFill(Color.GREEN);
			int radius = 2;
			for (int i=0; i<xpoints.length; i++) {
				g.fillRect(xp[i] - radius, yp[i] - radius, radius, radius);
			}
		}
	}
	
	public void drawPolygonWithFrame(GraphicsContext g) {
		drawPolygon(g, c);
		g.setStroke(f);
		g.setLineWidth(1);
		g.strokePolygon(xp, yp, 4);
        g.stroke();
	}
	
	public void setFrameColor(Color frameColor) {
		this.f = frameColor;
	}

	
	public void setDrawFrameOnly(boolean drawFrameOnly) {
		this.drawFrameOnly = drawFrameOnly;
	}
	
	public double[] getCenterPoint() {
		double newx = 0, newy = 0, newz = 0;
		for (int i=0; i<x.length; i++) {
			newx += x[i];
			newy += y[i];
			newz += z[i];
		}
		newx /= 4;
		newy /= 4;
		newz /= 4;
		return new double[] {newx, newy, newz};
		
	}
	
	public void toggleSelect(boolean selected) {
		this.selected = selected;
	}
	
	public void moveOnXYZ(Point3D increments) {
		moveOnX(increments.getX());
		moveOnY(increments.getY());
		moveOnZ(increments.getZ());
	}
	
	private void moveOnX(double increment) {
		for (int i=0; i<x.length; i++) {
			this.x[i] += increment;
		}
	}
	
	private void moveOnY(double increment) {
		for (int i=0; i<y.length; i++) {
			this.y[i] += increment;
		}
	}
	
	private void moveOnZ(double increment) {
		for (int i=0; i<z.length; i++) {
			this.z[i] += increment;
		}
	}

	
	public boolean isSelected() {
		return selected;
	}
	
	public double[] getX() {
		// return set of x coordinates for all points
		return x;
	}
	
	public double[] getY() {
		// return set of y coordinates for all points
		return y;
	}
	
	public double[] getZ() {
		// return set of z coordinates for all points
		return z;
	}
	
	public void setColor(Color c) {
		this.c = c;
	}
	
	public double getAvgDist() {
	   /* return average of distances from all points on polygon
		* to camera's view from. used to order the drawing of shapes
		* from furthest object to nearest object */
		return avgDist;
	}
	
	public void setAvgDist(double avgDist) {
			this.avgDist=avgDist;
		}

	public int compareTo(Poly o2) {
	    if (this.getAvgDist()<o2.getAvgDist()) {
	          return 1;
	    } else if (o2.getAvgDist()<this.getAvgDist()) {
	          return -1;
	    }
	    return 0;
	}
	
	public int getShapeNumber() {
		return shapeNumber;
	}
	
	public void rotatePoly(int degrees) {}
		
		
	public boolean isTestInstance() {
		return isTestInstance;
	}
		
	public void setTestInstance(boolean isTestInstance) {
		this.isTestInstance = isTestInstance;
	}
		

}