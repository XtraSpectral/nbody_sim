package main.objects;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.gui.Main;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

/**Vector is multipurpose, representing a line in space
 * with magnitude in directions (x,y,z) at a bare minimum,
 * but can go on to include positional and directional information.
 * Meaningful Vectors include line-of-sight view.
 * A vector's directionality is from its origin towards a foci. */
public class Camera {
	
	private Point3D xyz; // ratio of distances between component directions
	private Point3D dxyz; // length of vector in component directions
	private Point2D xy;
	private Point3D viewFrom = null; // [x,y,z] of a point
	private Point3D viewTo = null; // [x,y,z] of a point
	private double distance;
	private double inclination;
	private double rotation;
	private double hypotenuseXY;
	private int polygonsInView;
	private Color color;
	private String label = null;

	// importable/exportable 
	private double animationModifier = 0.0;
	private double oldDistance;
	
	
	public Camera(Point3D vector) {
		this(new Point3D(0, 0, 0), vector);
	}
	
	public Camera(Point3D a, Point3D b) {		
		
		xyz = a.subtract(b);
		viewTo = a;
		viewFrom = b;
		
		double x = xyz.getX();
		double y = xyz.getY();
		double z = xyz.getZ();
		
		// if absolute value isnt used, distance will become NaN
		distance = Math.sqrt(Math.abs(x * x + y * y + z * z));
		
		if (distance>0) {
			dxyz = xyz;
			this.xyz = xyz.normalize();
		}
	}
	
	public Camera atOrigin() {
		return null;
	}
	
	public void setOrigin(double ox, double oy, double oz) {
		viewTo = new Point3D(ox, oy, oz);
		viewFrom = viewTo.add(this.xyz);
	}
	
	
	public void drawVector(GraphicsContext gc, Camera viewVector) {
		Point2D point1 = viewVector.transformPointTo2D(viewTo);
		Point2D point2 = viewVector.transformPointTo2D(viewFrom);
		gc.setStroke(color==null ? Color.WHITE : color);
		gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
		
		// draw start / view to / negative side of axis
		gc.setFill(Color.GREEN);
		gc.fillOval(point1.getX()-4, point1.getY()-4, 8, 8);
		
		// draw end / view from / positive side of axis
		gc.setFill(Color.BLUE);
		gc.fillOval(point2.getX()-4, point2.getY()-4, 8, 8);
		if (label != null) {
			gc.setFill(Color.WHITE);
			gc.fillOval(point2.getX()-2, point2.getY()-2, 4, 4);
			gc.fillOval(point1.getX()-2, point1.getY()-2, 4, 4);
			gc.setFont(new Font("Consolas", 14));
			String coords = String.format("%.1f:%.1f:%.1f", viewFrom.getX(), viewFrom.getY(), viewFrom.getZ());
			gc.fillText(coords, point2.getX()+8, point2.getY()-12);
		}
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	
	public void drawVectorAnimation(Camera view, GraphicsContext g) {
		
		// animation settings
		double length = 1;
		final double maxAnimLength = length * 2;
		
		// blueshift if shrinking, redshift if growing
		final double distanceDelta = distance - oldDistance;
		final Color c = (distanceDelta >= 0 ? Color.RED : Color.BLUE);
		
		// x,y,z coordinates of vector animation's origination point
		double px = distanceDelta >= 0 ? viewFrom.getX() : viewTo.getX();
		double py = distanceDelta >= 0 ? viewFrom.getY() : viewTo.getY();
		double pz = distanceDelta >= 0 ? viewFrom.getZ() : viewTo.getZ();
		double d = (distanceDelta >= 0 ? 1 : -1);
		
		for (double i=0; i<distance; i+=(length*2)) {
			
			// continue if new line segment begins past vector's endpoint
			if (i+animationModifier >= distance) {
				continue;
			}
			
			// draw additional, truncated line segment if animation offset
			// is greater than vector length
			if (i==0.0 && animationModifier>length) {
				Point3D newPointA3D = new Point3D(px, py, pz);
				Point3D newPointB3D = new Point3D(
						px+((animationModifier-length)*xyz.getX()*d), 
						py+((animationModifier-length)*xyz.getY()*d), 
						pz+((animationModifier-length)*xyz.getZ()*d));
				
				Point2D newPointA2D = view.transformPointTo2D(newPointA3D);
				Point2D newPointB2D = view.transformPointTo2D(newPointB3D);

				g.setStroke(c);
				g.strokeLine((int) newPointA2D.getX(), (int) newPointA2D.getY(), 
						   (int) newPointB2D.getX(), (int) newPointB2D.getY());
			}
			
			if (i+animationModifier+length > distance) {
				// if the end of the line segment is past the vector endpoint
				length = distance - i - animationModifier;
				// then the length of that segment should
				assert (length > 0.0);
			}

			Point3D newPointA3D = new Point3D(
					px+((i+animationModifier)*xyz.getX())*d, 
					py+((i+animationModifier)*xyz.getY())*d, 
					pz+((i+animationModifier)*xyz.getZ())*d);
			Point3D newPointB3D = new Point3D(
					px+(((i+animationModifier)*xyz.getX())+(length*xyz.getX()))*d, 
					py+(((i+animationModifier)*xyz.getY())+(length*xyz.getY()))*d, 
					pz+(((i+animationModifier)*xyz.getZ())+(length*xyz.getZ()))*d);
			
			Point2D newPointA2D = view.transformPointTo2D(newPointA3D);
			Point2D newPointB2D = view.transformPointTo2D(newPointB3D);

			g.setStroke(c);
    		g.setLineWidth(1);
			g.strokeLine((int) newPointA2D.getX(), (int) newPointA2D.getY(), 
					   (int) newPointB2D.getX(), (int) newPointB2D.getY());
		}
		
		animationModifier += (.04);
		if (animationModifier >= maxAnimLength || animationModifier <= maxAnimLength * -1) {
			animationModifier = 0;
		}

	}
	
	public void resetPosition() {
		this.viewFrom = new Point3D(20, 20, 30);
		this.viewTo = new Point3D(0, 0, 0);
	}

	
	public double getAvgDistanceFromOrigin(double[] x, double[] y, double[] z) {
		// returns average distance between multiple 3d points, and this vector's origin
		double total = 0;
		for (int i=0; i<x.length; i++)
			total += getDistanceFromOrigin(x[i], y[i], z[i]);
		double avgDist = total/x.length;
		try {
			assert (!Double.isNaN(avgDist));
		} catch (AssertionError e) {
			System.exit(0);
		}
		
		return avgDist;
	}
	
	public double getDistanceFromOrigin(double x, double y, double z) {
		// returns distance between 3d point and this vector's origin
		
		double dist = Math.sqrt((viewFrom.getX() - x)*(viewFrom.getX() - x) +
					  (viewFrom.getY() - y)*(viewFrom.getY() - y) +
					  (viewFrom.getZ() - z)*(viewFrom.getZ() - z));

		return dist;
	}
		
	public void setPerspective2D(Scene scene) {
		// calibrate center of 3d view to center of GUI
		// confirmed to be the correct procedure
		// WARNING this offset is artifact of physics render control panel
		Point2D sceneGeo = new Point2D(scene.getWidth()/2-110, scene.getHeight()/2);
		Point2D point2D = make2D(viewTo);
		this.xy = point2D.add(sceneGeo);
		triangulateVector();
	}
	
	public void setPerspective2D(double width, double height) {
		// calibrate center of 3d view to center of GUI
		// confirmed to be the correct procedure
		Point2D sceneGeo = new Point2D(width/2, height/2);
		Point2D point2D = make2D(viewTo);
		this.xy = point2D.add(sceneGeo);
		triangulateVector();
	}
	
	public Point2D transformPointTo2D(Point3D xyz) {
		// transform 3d point to 2d, then calibrate to position on GUI
		Point2D point2D = make2D(xyz);
		return point2D.multiply(50).add(xy);
	}

	private Point2D make2D(Point3D point3D) {
		// make a 2d point from a 3d point
		double drawX = 0.0, drawY = 0.0;
		Camera viewVector = new Camera(viewTo, viewFrom);
		Camera rotationVector = getRotationVector();
		Camera WeirdVector1 = viewVector.CrossProduct(rotationVector);
		Camera WeirdVector2 = viewVector.CrossProduct(WeirdVector1);
		Camera ViewToPoint = new Camera(point3D, viewFrom);
		
		double t = (viewVector.x() * viewTo.getX() + viewVector.y()*viewTo.getY() + viewVector.z()*viewTo.getZ()
			   	-  (viewVector.x() * viewFrom.getX() + viewVector.y()*viewFrom.getY() + viewVector.z()*viewFrom.getZ()))
				/  (viewVector.x() * ViewToPoint.x() + viewVector.y()*ViewToPoint.y() + viewVector.z()*ViewToPoint.z());
		
		double xNew = viewFrom.getX() + ViewToPoint.x() * t;
		double yNew = viewFrom.getY() + ViewToPoint.y() * t;
		double zNew = viewFrom.getZ() + ViewToPoint.z() * t;
		
		if(t > 0) {
			drawX = WeirdVector2.x() * xNew + WeirdVector2.y() * yNew + WeirdVector2.z() * zNew;
			drawY = WeirdVector1.x() * xNew + WeirdVector1.y() * yNew + WeirdVector1.z() * zNew;
		}
		return new Point2D(drawX, drawY);
	}
	
	private Camera getRotationVector() {
		// returns a 2-dimensional unit vector from the vector along the view from point to
		// the view to point. the resultant vector has the same directionality as the input 
		// vector and represents a tranformation into the view vector's space
		
		// create a unit vector from the relative position of viewFrom's x, y coords
		// along their respective axis, where |x|+|y|=1, and x, y are real numbers.
		// moving the viewFrom point will change the output vector
		
		// get ratios and create unit vector components
		double dx = Math.abs(viewFrom.getX()-viewTo.getX());
		double dy = Math.abs(viewFrom.getY()-viewTo.getY());
		double xUnit = dy/(dx+dy);		
		double yUnit = dx/(dx+dy);
		
		// flip along axis if necessary, for new vector directionality
		if(viewFrom.getY()>viewTo.getY()) {xUnit = -xUnit;}
		if(viewFrom.getX()<viewTo.getX()) {yUnit = -yUnit;}

		return new Camera(new Point3D(xUnit, yUnit, 0));
	}
	
	public Point2D transformRelativePointTo2D(Point3D xyz) {
		return transformPointTo2D(viewTo.add(xyz));
	}
	
	public Camera CrossProduct(Camera V) {	
		return new Camera(xyz.crossProduct(V.xyz));
	}
	
	public void zoom(double magnitude) {
		// move both points of this vector along this vector
		Point3D temp = this.xyz.multiply(magnitude);
		this.viewFrom = this.viewFrom.add(temp);
		this.viewTo = this.viewTo.add(temp);
	}
	
	public void pan(double magnitude) {
		Camera verticalVector = new Camera(new Point3D(0,0,1));
		Camera sideViewVector = CrossProduct(verticalVector);
		Point3D temp = sideViewVector.xyz.multiply(magnitude);
		this.viewFrom = this.viewFrom.add(temp);
		this.viewTo = this.viewTo.add(temp);
	}
	
	public void moveForwardWithFocus() {
		double a = viewFrom.getX()-viewTo.getX();
		double b = viewFrom.getY()-viewTo.getY();
		double c = Math.sqrt(Math.abs(a * a + b * b));
		double step = a/c;
		double theta = Math.asin(step); // in radians, possibly for future use
		
		// TODO not sure why this works
		// may have to invert some signs if copying this method to move backward
		int h = b/c > 0 ? -1 : 1;
		this.viewFrom = new Point3D(viewFrom.getX()-Math.cos(theta),viewFrom.getY()+Math.sin(theta)*h,viewFrom.getZ());
	}

	public void moveOnAxis(Point3D increment) {
		// move origin along (x, y, z) axis while focusing on some point
		this.viewFrom = this.viewFrom.add(increment);
	}
	
	public double getXComponent() {
		return this.dxyz.getX();
	}
	
	public double getYComponent() {
		return this.dxyz.getY();
	}
	
	public double getZComponent() {
		return this.dxyz.getZ();
	}
	
	public Point3D getViewFrom() {
		return viewFrom;
	}
	
	public Point3D getViewTo() {
		return viewTo;
	}
	
	public double x() {
		return this.xyz.getX();
	}
	
	public double y() {
		return this.xyz.getY();
	}
	
	public double z() {
		return this.xyz.getZ();
	}
	
	public double getAnimationModifier() {
		return animationModifier;
	}
		
	public String toString() {
		return String.valueOf(xyz.getX()) + " " + String.valueOf(xyz.getY()) + " " + String.valueOf(xyz.getZ());
	}

	
	/* ========================================== */
	/* ===== IMPORTING AND EXPORTING FIELDS ===== */
	/* ========================================== */
	
	public void importVector(Camera v) {
		this.animationModifier = v.getAnimationModifier();
		this.oldDistance = v.getDistance();
	}
	
	/* =============================================== */
	/* ===== TRIANGULATION AND SPATIAL MECHANICS ===== */
	/* =============================================== */
	
	// collect spatial positioning data, usually for output to a gui or interface
	public void triangulateVector() {
		double x = viewFrom.getX() - viewTo.getX();
		double y = viewFrom.getY() - viewTo.getY();
		double z = viewFrom.getZ() - viewTo.getZ();
		
		this.hypotenuseXY = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double r = Math.atan(y / x);
		if (x < 0.0) {										
			r = Math.PI + r; // flip on x
		}
		if (y < 0.0 && x >= 0) {
			r = 2 * Math.PI + r; // flip on x
		}
		this.rotation = r; 
		this.inclination = Math.atan(z/hypotenuseXY);
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getInclination() {
		return inclination;
	}
	
	public double getRotation() {
		return rotation;
	}
	
	
	public int getPolygonsInView() {
		return polygonsInView;
	}

	public void setPolygonsInView(int polygonsInView) {
		this.polygonsInView = polygonsInView;
	}

	
}