package main.objects;

import main.gui.Gradient;
import main.laws.Physics;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javafx.geometry.Point3D;

public class PhysicsNode extends Node {
	
	private int rotation = 0; // degrees
	
	// net *acceleration* due to gravity (m/s2) THIS IS NOT FORCE FROM GRAVITY
	// multiply by mass (F = ma) to get gravitational force in N or kg/m2
	private Point3D sumGForce; 
	private boolean hasOutline = false;
	
	// initialized in constructor
	private double mass; // mass (kg)
	private Point3D velocity;
	private Point3D acceleration;
	
	
	// shape details
	private Point3D oldXYZ;

	
	private int sumKTemps = 0;
	private double albedo;
	private double emmisivity;
	private int kelvin; // temperature (k)
	private double radius; // object radius (km)
	private String type; // all lowercase star or planet
	private boolean tempRenderable = true;


	public PhysicsNode(Point3D xyz, int id) {
		this(xyz, id, "planet", 0, .07,
			 ThreadLocalRandom.current().nextInt(6000, 60000),
			 ThreadLocalRandom.current().nextDouble(1e23, 1e26));
	}
	
	public PhysicsNode(Point3D xyz, int id, String type, int kelvin, double size, int radius, double mass) {
		super(xyz, id, size);
		this.kelvin = kelvin;
		this.type = type;
		this.radius = radius;
		this.albedo = ThreadLocalRandom.current().nextDouble(0.1,0.5);
		this.emmisivity = ThreadLocalRandom.current().nextDouble(0.0,0.7);
		this.mass = mass;
		renderAsCube();
		this.velocity = new Point3D(
				ThreadLocalRandom.current().nextInt(-30000, 30000)/Physics.scaleAU,
				ThreadLocalRandom.current().nextInt(-30000, 30000)/Physics.scaleAU,
				ThreadLocalRandom.current().nextInt(-100, 100)/Physics.scaleAU
				);
		
		if (type.equals("star")) {
			this.velocity = new Point3D(
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU,
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU,
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU
					);
		} 
		
	}
	
	public int getKelvin() {
		return kelvin;
	}
	
	public void setKelvin(int kelvin) {
		this.kelvin = kelvin;
	}
	
	public void scaleKelvin(double scale) {
		kelvin *= (1 + scale);
	}

	
	public void resetKelvin() {
		if (this.type.equals("star")) {
			kelvin = ThreadLocalRandom.current().nextInt(7000, 9000);
		} else {
			kelvin = 0;
		}
	}
	
	public double getRadius() {
		return radius;
	}
	
	
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	
	public void applyTempFrom(PhysicsNode p) {
		if (getType().equals("planet") && p.getType().equals("star")) {

			Camera v = new Camera(p.getXYZ(), getXYZ());
			// AU distance between objects, used in each grav force and temperature calculations
			double d = v.getDistance()*Physics.scaleAU;
			
			double sT = p.getKelvin();
			double sR = p.getRadius()*1000.0; // convert to meters
			double pA = albedo;
			double pE = emmisivity;
			
			double inner = sR * Math.sqrt( 1 - pA - pE / 2 );
			double outer = inner / (2 * d);
			int pT = (int) (Math.sqrt(outer) * sT);			
			
			if (pT > sumKTemps) {this.sumKTemps = pT;}
		}
	}
	
	public void applyEnergy() {
		
		// apply new temperature for planets only, and get new color
		if (type.equals("planet")) {
			kelvin = sumKTemps;
			sumKTemps = 0;
		}
		changeColor(Gradient.getColor(kelvin));
	}
	
	public String getType() {
		return type;
	}
	
	public void turnOffThisRenderCycle() {
		this.tempRenderable = false;
	}
	
	public void turnOnNextRenderCycle() {
		if (!tempRenderable) {this.tempRenderable = true;}
	}
	
	
	public double getEmission() {
		return emmisivity;
	}

	public double getAlbedo() {
		return albedo;
	}
	
	
	public void applyForcesFrom(List<PhysicsNode> allObjects) {
		// apply the forces from each PhysicsNode in argument that isn't this node
		allObjects.stream().filter(pn -> !pn.equals(this))
						   .forEach(pn -> applyForceFrom(pn));
	}

	public void applyForceFrom(PhysicsNode o) {

		// EVERYTING WITHIN THIS SCOPE MUST BE CALCULATED TO-SCALE, IN METERS
		
		// update this object's position based on grav influence of another
		Camera v = new Camera(o.getXYZ(), getXYZ());
		// AU distance between objects, used in each grav force and temperature calculations
		double d = v.getDistance()*Physics.scaleAU;
		
		Point3D vector = o.getXYZ().subtract(getXYZ());
		Point3D normalized = vector.normalize();

		// gravitational acceleration between this and other object. N or kg/m3
		// any number manipulated against g must be to scale!
		// changelog 1-7-17: removed this.mass from numerator of g, because g is acceleration
		// so mass is divided out (Constant.G * mass * o.getMass()) / Math.pow(d, 3) / mass
		double g = (Physics.G * o.getMass()) / Math.pow(d, 3);


		if (Double.isInfinite(g)) {
			g = 0;
		}
		
		

		// break down acceleration into its components
//		sumGForceX += (g * v.getX());
//		sumGForceY += (g * v.getY());
//		sumGForceZ += (g * v.getZ());
		this.sumGForce = normalized.multiply(g);


		

		// TODO TODO TODO TODO TODO TODO
		//
		// APPLY LIMITS OF THE SPEED OF LIGHT TO MOVEMENT
		// WHERE DOES IT NEED TO BE IMPLEMENTED HERE?
		// ARE THERE LIMITS THAT NEED TO BE PLACED ON ACCELERATION TOO?
		// THIS IS NECESSARY FOR SOLVING TOO-FAST MOVEMENT IN THE SIMULATION
		//
		// FIXME FIXME FIXME FIXME FIXME

	}


	
	public void applyAllForces() {

		// apply gravitational acceleration, scaled down to digital grid
		// multiple by timescale to convert AU/s2 to AU/day2
		this.acceleration = this.sumGForce;

		// change in velocity. a = (v2 - v1) / t, so v2 = v1 + (a * t)
		this.velocity = this.velocity.add(this.acceleration.multiply(Physics.timeScale));

		// change in position. v = (d2 - d1) / t, so d2 = d1 + (v * t)
		// apply timescale adjustment here, because the object will move at this method's
		// final calculated velocity at each second for the timeScale's period of time
		Point3D movements = this.velocity.multiply(Physics.timeScale);
		moveShape(movements);
		
		// apply new temperature for planets only, and get new color
		applyEnergy();
		
		// outline object if color is too dark
		hasOutline = (getKelvin() < Gradient.COLOR_SCALE * 1.4 ? true : false);
		
		// TODO object visual rotation adjusted here
		rotateShape(++rotation);
		if (rotation==359) {rotation=0;}

		// reset sum forces where needed
		sumGForce = new Point3D(0, 0, 0);
	}
	
	public void applyNewForces(Point3D xyz) {
		this.velocity = this.velocity.add(xyz);
	}

	public double getMass() {
		return mass;
	}
	
	public void velocityMultiplier(double multiplier) {
		this.velocity = this.velocity.multiply(multiplier);

	}

	
	public Camera showVectorTo(PhysicsNode o) {
		return new Camera(getXYZ(), o.getXYZ());
	}

	public void setType(String type) {
		setType(type);
		if (type.equals("star")) {
			setKelvin(8000);
			this.velocity = new Point3D(
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU,
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU,
					ThreadLocalRandom.current().nextInt(-10, 10)/Physics.scaleAU
					);
			mass = 1.989e30;
			setRadius(695700);
		}
	}

	public void moveShape(Point3D xyz) {
		// select each polygon in shape and move it by (x,y,z)	
		getPolygons().stream().forEach(p -> p.moveOnXYZ(xyz));
				
		oldXYZ = getXYZ();
		setXYZ(getXYZ().add(xyz));
	}
	

	public void expandSpaceAroundShape(double multiplier) {
		
		oldXYZ = getXYZ();
		
		setXYZ(getXYZ().multiply(multiplier));
		
		Point3D movements = getXYZ().subtract(oldXYZ);
		getPolygons().stream().forEach(p -> p.moveOnXYZ(movements));		
	}

	public double[] getMovementDelta() {
		return new double[] {getXYZ().getX() - oldXYZ.getX(), 
				getXYZ().getY() - oldXYZ.getY(), getXYZ().getZ() - oldXYZ.getZ()};
	}
	

	
	public void rotateShape(int degrees) {
		// TODO not currently implemented
		// for now, just work on rotating top and bottom polygons
		getPolygons().get(0).rotatePoly(degrees);
		getPolygons().get(1).rotatePoly(degrees);
	}

	
	public Point3D getXYZVelocity() {
		return velocity;
	}
	
	public Point3D getXYZAcceleration() {
		return acceleration;
	}
	
//	public boolean containsPoint3D(double[] point) {
//		boolean onX = (point[0] >= x && point[0] <=x+l ? true : false);
//		boolean onY = (point[1] >= y && point[1] <=y+l ? true : false);
//		boolean onZ = (point[2] >= z && point[2] <=z+l ? true : false);	
//		
//		return (onX && onY && onZ);
//	}




}
