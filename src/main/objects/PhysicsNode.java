package main.objects;

import main.gui.Gradient;
import main.laws.Physics;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point3D;


public class PhysicsNode extends Node {
	
	/**Extends Node with effect for physical forces: gravity and radiation.
	 */

	// net *acceleration* due to gravity (m/s2) -- NOT FORCE FROM GRAVITY --
	// multiply by mass (F = ma) to get gravitational force in N or kg/m2
	private Point3D sumGForce; 
	
	private double mass; // mass (kg)
	private Point3D velocity;
	private Point3D acceleration;
	
	private int sumKTemps = 0;
	private double albedo;
	private double emmisivity;
	private int kelvin; // temperature (k)
	private int defaultKelvin;
	private double radius; // object radius (km)
	private String type; // all lowercase star or planet


	public PhysicsNode(Point3D xyz, int id) {
		this(xyz, id, "planet", 0, .07,
			 ThreadLocalRandom.current().nextInt(6000, 60000),
			 ThreadLocalRandom.current().nextDouble(1e23, 1e26));
	}
	
	public PhysicsNode(Point3D xyz, int id, String type, int kelvin, double size, int radius, double mass) {
		super(xyz, id, size);
		this.kelvin = kelvin;
		this.defaultKelvin = kelvin;
		this.type = type;
		this.radius = radius;
		this.mass = mass;
		this.albedo = ThreadLocalRandom.current().nextDouble(0.1,0.5);
		this.emmisivity = ThreadLocalRandom.current().nextDouble(0.0,0.7);
		this.sumGForce = new Point3D(0, 0, 0);
		asCube();
		resetVelocity();
	}
	
	public int getKelvin() {
		return kelvin;
	}
	
	public void setKelvin(int kelvin) {
		this.kelvin = kelvin;
		this.defaultKelvin = kelvin;
	}
	
	public void scaleKelvin(double scale) {
		kelvin = (int) (defaultKelvin * scale);
	}
	
	public void resetKelvin() {
		if (this.type.equals("star")) {
			kelvin = ThreadLocalRandom.current().nextInt(7000, 11000);
		} else {
			kelvin = 0;
		}
	}
	
	public void resetVelocity() {
		int range = this.type.equals("star") ? 10 : 20000;
		this.velocity = new Point3D(
				ThreadLocalRandom.current().nextInt(-range, range) / Physics.scaleAU,
				ThreadLocalRandom.current().nextInt(-range, range) / Physics.scaleAU,
				ThreadLocalRandom.current().nextInt(-range / 2, range / 2) / Physics.scaleAU
				);
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void applyTempFrom(PhysicsNode p) {
		if (getType().equals("planet") && p.getType().equals("star")) {

			Vector v = new Vector(p.getXYZ(), getXYZ());
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
		setColor(Gradient.getColor(kelvin));
	}

	
	public void applyForcesFrom(List<PhysicsNode> allObjects) {
		// apply the forces from each PhysicsNode in argument that isn't this node
		allObjects.stream().filter(pn -> !pn.equals(this))
						   .forEach(pn -> applyForceFrom(pn));
	}

	public void applyForceFrom(PhysicsNode o) {
		// EVERYTING WITHIN THIS SCOPE MUST BE CALCULATED TO-SCALE, IN METERS
		
		// AU distance between objects, used in each grav force and temperature calculations
		double d = o.getXYZ().distance(getXYZ()) * Physics.scaleAU;
		Point3D vector = o.getXYZ().subtract(getXYZ());

		// gravitational acceleration between this and other object. N or kg/m3
		// any number manipulated against g must be to scale!
		// changelog 1-7-17: removed this.mass from numerator of g, because g is acceleration
		// so mass is divided out (Constant.G * mass * o.getMass()) / Math.pow(d, 3) / mass
		double g = (Physics.G * o.getMass()) / Math.pow(d, 3);
		if (Double.isInfinite(g)) {
			g = 0;
		}

		// break down acceleration into its components
		this.sumGForce = this.sumGForce.add(vector.multiply(g));
		// Point3D normalized = vector.normalize();
		// this.sumGForce = this.sumGForce.add(normalized.multiply(g));
	}

	public void applyAllForces() {

		// apply gravitational acceleration, scaled down to digital grid
		// multiple by timescale to convert AU/s2 to AU/day2
		this.acceleration = this.sumGForce;

		// change in velocity. a = (v2 - v1) / t, so v2 = v1 + (a * t)
		this.velocity = this.velocity.add(this.acceleration.multiply(Physics.timeScale));
		
		boolean xValid = Math.abs(this.velocity.getX()) < Physics.C;
		boolean yValid = Math.abs(this.velocity.getY()) < Physics.C;
		boolean zValid = Math.abs(this.velocity.getZ()) < Physics.C;
		
		if (!xValid | !yValid | !zValid) {
			this.velocity = new Point3D(
					(xValid ? this.velocity.getX() : Physics.C * (this.velocity.getX() < 0 ? -1 : 1)),
					(yValid ? this.velocity.getY() : Physics.C * (this.velocity.getY() < 0 ? -1 : 1)),
					(zValid ? this.velocity.getZ() : Physics.C * (this.velocity.getZ() < 0 ? -1 : 1))
					);
		}
		
		// change in position. v = (d2 - d1) / t, so d2 = d1 + (v * t), or delta_d = v * t
		// apply timescale adjustment here, because the object will move at this method's
		// final calculated velocity at each second for the timeScale's period of time
		Point3D movements = this.velocity.multiply(Physics.timeScale);
		moveShape(movements);
		
		// apply new temperature for planets only, and get new color
		applyEnergy();
		
		// reset sum forces 
		sumGForce = new Point3D(0, 0, 0);
	}
	
	public void velocityMultiplier(double multiplier) {
		this.velocity = this.velocity.multiply(multiplier);

	}

	public void setType(String type) {
		this.type = type;
		if (type.equals("star")) {
			setKelvin(ThreadLocalRandom.current().nextInt(3500, 12000));
			resetVelocity();
			this.mass = 1.989e30;
			this.radius = 695700;
		}
	}

	public void moveShape(Point3D xyz) {
		// select each polygon in shape and move it by (x,y,z)	
		getPolygons().stream().forEach(p -> p.moveOnXYZ(xyz));
		setXYZ(getXYZ().add(xyz));
	}
	
	public void expandSpaceAroundShape(double scale) {
		Point3D movement = getXYZ().multiply(scale).subtract(getXYZ());
		moveShape(movement);
	}
		
	public Point3D getXYZVelocity() {
		return velocity;
	}
	
	public Point3D getXYZAcceleration() {
		return acceleration;
	}

	public double getMass() {
		return mass;
	}
	
	public String getType() {
		return type;
	}
}
