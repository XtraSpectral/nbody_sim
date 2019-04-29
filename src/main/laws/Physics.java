package main.laws;

public class Physics {
	
	/* Class of physics constants and simulation environment variables
	 * 
	 */
	
	// gravitational constant
	private static final double defaultG = 6.67428e-11;
	
	// gravitational constant in simulation
	public static double G = defaultG;
	
	// one AU in meters
	public static final double AU = 149.59e9;

	// scale is AU in meters, divided by a zoom amount
	public static double scaleAU = AU / 1;
	
	// time period that each simulation refresh represents, in seconds
	public static final double timeScale = 3600 * 24; // default 24*3600 (seconds/day)
	
	// speed of light in km/h, to au/h, to au/s 
	// -- this is an approximation for the correct scale needed for calculations, --
	// -- like in PhysicsNode force calculation scopes --
	public static final double C = 1.079e+9 / AU / 60 / 60;
	
	public static void resetG() {
		G = defaultG;
	}
	
	public static double scaleG(double scale) {
		G = defaultG * scale;
		return G;
	}
	
}

