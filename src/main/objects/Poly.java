package main.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import java.util.stream.IntStream;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;

public class Poly extends Polygon {
	
	/**Polygon class for rendering 3D geometric objects in 2D.
	 * 
	 * Each Polygon has characteristics for color, 3D position, 2D position,
	 * and distance from the focus of the application's camera vector.
	 * Polygons are associated with a node with shape data, unless
	 * drawn freely.
	 */
	
	private Color c; // color of the drawable graphic's body
	private double[] x, y, z; // default coordinates for each vertex
	private double distance = 0.0; // render distance from camera's focus
	private boolean selected = false; // is the parent object selected?
	private int shapeNumber; // identifier for parent object
	
	// x points and y points for 2D screen placement
	// even though the Polygon class stores the same values, having independent
	// sets for 2D point (x, y) is more convenient to render and work with.
	// However, its easier to check for mouseclicks on Polygons with the Polygon class
	// Dumb but easy
	private double[] xp, yp; 
	
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
	}
	
	public double getDistanceFromPoint(Point3D point) {
		// returns average distance between this polygons vertices and a point
		double distance = IntStream.range(0, x.length)
							   	   .mapToDouble(i -> point.distance(new Point3D(x[i], y[i], z[i])))
							       .average()
							       .getAsDouble();
		return distance;
	}

	
	public void update(Vector camera) {
		this.getPoints().clear();
		
		xp = new double[4];
		yp = new double[4];
		
		for (int i=0; i<4; i++) {
			Point2D point2D = camera.toScreenSpace(new Point3D(x[i], y[i], z[i]));
			// add new point to the Polygon's and Poly's point sets
			getPoints().addAll(point2D.getX(), point2D.getY());
			xp[i] = point2D.getX();
			yp[i] = point2D.getY();
		}

		this.distance = getDistanceFromPoint(camera.getOrigin());
	}
	
	public void draw(GraphicsContext g) {
		
		g.setFill(c);
		g.fillPolygon(xp, yp, 4);
		g.fill();
        g.stroke();
		
		// draw selection details
		if (selected) {
			// green pixels on corner
			g.setFill(Color.GREEN);
			int radius = 2;
			for (int i=0; i<xp.length; i++) {
				g.fillRect(xp[i] - radius, yp[i] - radius, radius, radius);
			}
		}
	}
	
	public void toggleSelect(boolean selected) {
		this.selected = selected;
	}
	
	public void moveOnXYZ(Point3D increments) {
		for (int i=0; i<x.length; i++) {
			this.x[i] += increments.getX();
			this.y[i] += increments.getY();
			this.z[i] += increments.getZ();
		}
	}

	public void setColor(Color c) {
		this.c = c;
	}
	
	public double getDistance() {
		return distance;
	}

	public int compareTo(Poly otherPoly) {
	    if (this.getDistance()<otherPoly.getDistance()) {
	          return 1;
	    } else if (otherPoly.getDistance()<this.getDistance()) {
	          return -1;
	    }
	    return 0;
	}
	
	public int getShapeNumber() {
		return shapeNumber;
	}
}