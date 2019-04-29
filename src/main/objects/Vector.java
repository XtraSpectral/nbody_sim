package main.objects;

import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;


public class Vector {
	
	/**A Vector is a multi-purpose toolkit for complex line problems. 
	 * They are used for cameras, animations, and representing spatial
	 * relationships.
	 */
	
	private Point3D xyz; // distances for component directions
	private Point2D xy; // 2D representation of the vector, used by cameras for screen-space translations
	private Point3D origin = null; // source of the vector
	private Point3D foci = null; // termination point of the vector
	private double distance;
	private double inclination;
	private double rotation;
	private double hypotenuseXY;
	private double animationModifier = 0.0; // used by animation sequence to offset renders
	private double oldDistance; // used by sequences for vector-to-vector comparisons (e.g. doppler shift)
	
	public static Vector defaultCamera() {
		return new Vector(new Point3D(0, 0, 0), new Point3D(20, 20, 30));
	}
	
	public Vector(Point3D vector) {
		this(new Point3D(0, 0, 0), vector);
	}
	
	public Vector(Point3D a, Point3D b) {		
		foci = a;
		origin = b;
		update();
	}
	
	public void drawVectorAnimation(Vector camera, GraphicsContext g) {
		// as an animation, requires a camera
		
		// animation settings
		double length = 1; // length of animated line segments
		final double maxAnimLength = length * 2; // total space for drawing a segment
		
		// blueshift if shrinking, redshift if growing
		final double distanceDelta = distance - oldDistance;
		final Color c = (distanceDelta >= 0 ? Color.RED : Color.BLUE);
		
		// x,y,z coordinates of vector animation's origination point
		double d = distanceDelta >= 0 ? 1 : -1;
		Point3D animOrigin = distanceDelta >= 0 ? origin : foci;
		
		for (double i=0; i<distance; i+=(length*2)) {
			
			// continue if new line segment begins past vector's endpoint
			if (i+animationModifier >= distance) {
				continue;
			}
			
			// draw additional, truncated line segment if animation offset
			// is greater than vector length
			if (i==0.0 && animationModifier>length) {
				Point3D newPointA3D = animOrigin;
				Point3D newPointB3D = xyz.multiply(animationModifier - length).multiply(d).add(animOrigin);
				
				Point2D newPointA2D = camera.toScreenSpace(newPointA3D);
				Point2D newPointB2D = camera.toScreenSpace(newPointB3D);

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


			Point3D newPointA3D = xyz.multiply(i + animationModifier).multiply(d).add(animOrigin);
			Point3D addDistance = xyz.multiply(length);
			Point3D newPointB3D = xyz.multiply(i + animationModifier).add(addDistance).multiply(d).add(animOrigin);
			
			Point2D newPointA2D = camera.toScreenSpace(newPointA3D);
			Point2D newPointB2D = camera.toScreenSpace(newPointB3D);

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
		this.origin = new Point3D(20, 20, 30);
		this.foci = new Point3D(0, 0, 0);
		update();
	}
	
	private void update() {
		this.xyz = foci.subtract(origin);
		this.distance = foci.distance(origin);
		if (distance>0) {
			this.xyz = xyz.normalize();
		}
	}
		
	public void setPerspective2D(Scene scene) {
		// calibrate center of 3d view to center of GUI
		// confirmed to be the correct procedure
		// WARNING this offset is artifact of physics render control panel
		Point2D sceneGeo = new Point2D(scene.getWidth()/2-110, scene.getHeight()/2);
		Point2D point2D = make2D(foci);
		this.xy = point2D.add(sceneGeo);
		triangulateVector();
	}
	
	public void setPerspective2D(double width, double height) {
		// calibrate center of 3d view to center of GUI
		// confirmed to be the correct procedure
		Point2D sceneGeo = new Point2D(width/2, height/2);
		Point2D point2D = make2D(foci);
		this.xy = point2D.add(sceneGeo);
		triangulateVector();
	}
	
	public Point2D toScreenSpace(Point3D xyz) {
		// transform 3d point to 2d, then calibrate to position on GUI
		Point2D point2D = make2D(xyz);
		return point2D.multiply(50).add(xy);
	}

	private Point2D make2D(Point3D point3D) {		
		Point3D unitVector = foci.subtract(origin).normalize();
		Point3D rotationVector = this.rotate();
		Point3D yVector = unitVector.crossProduct(rotationVector);
		Point3D xVector = unitVector.crossProduct(yVector);
		Point3D vectorToPoint = point3D.subtract(origin).normalize();
		
		double perspective = (unitVector.dotProduct(foci) -  unitVector.dotProduct(origin))
				/  unitVector.dotProduct(vectorToPoint);

		Point3D newXYZ = origin.add(vectorToPoint.multiply(perspective));
		
		double x2D = 0.0, y2D = 0.0;
		if(perspective > 0) {
			x2D = xVector.dotProduct(newXYZ);
			y2D = yVector.dotProduct(newXYZ);
		}
		return new Point2D(x2D, y2D);
	}
	
	private Point3D rotate() {
		// create a rotation vector for this vector
		
		// get ratios and create unit vector components
		double dx = Math.abs(origin.getX()-foci.getX());
		double dy = Math.abs(origin.getY()-foci.getY());
		double xUnit = dy / (dx + dy);		
		double yUnit = dx / (dx + dy);
		
		// flip along axis if necessary, for new vector directionality
		if(origin.getY() > foci.getY()) {xUnit = -xUnit;}
		if(origin.getX() < foci.getX()) {yUnit = -yUnit;}

		return new Point3D(xUnit, yUnit, 0);
	}
	
	public Point2D transformRelativePointTo2D(Point3D xyz) {
		return toScreenSpace(foci.add(xyz));
	}
	
	
	public void dolly(double magnitude) {
		// move along camera vector of distance d by a constant factor
		Point3D temp = this.xyz.multiply(magnitude);
		this.origin = this.origin.add(temp);
		this.foci = this.foci.add(temp);
	}
	
	public void pan(double magnitude) {
		Point3D verticalVector = new Point3D(0,0,1);
		Point3D sideViewVector = xyz.crossProduct(verticalVector);
		Point3D temp = sideViewVector.multiply(magnitude);
		this.origin = this.origin.add(temp);
	}
	
	public void moveForwardWithFocus() {
		double a = origin.getX()-foci.getX();
		double b = origin.getY()-foci.getY();
		double c = Math.sqrt(Math.abs(a * a + b * b));
		double step = a/c;
		double theta = Math.asin(step); 
		
		// TODO not sure why this works
		// may have to invert some signs if copying this method to move backward
		int h = b/c > 0 ? -1 : 1;
		this.origin = new Point3D(origin.getX()-Math.cos(theta),origin.getY()+Math.sin(theta)*h,origin.getZ());
	}

	public void moveOnAxis(Point3D increment) {
		// move origin along (x, y, z) axis while focusing on some point
		this.origin = this.origin.add(increment);
	}
	
	public Point3D getOrigin() {
		return origin;
	}
	
	public Point3D getFoci() {
		return foci;
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
	
	public void setAnimationModifier(double mod) {
		animationModifier = mod;
	}
	
	public void setOldDistance(double dist) {
		this.oldDistance = dist;
	}
	
	/* =============================================== */
	/* ===== TRIANGULATION AND SPATIAL MECHANICS ===== */
	/* =============================================== */
	
	// collect spatial positioning data, usually for display
	public void triangulateVector() {
	
		Point3D vector = origin.subtract(foci);
		double x = vector.getX(); double y = vector.getY(); double z = vector.getZ();
		
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
	
	
}