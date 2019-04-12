package main.laws;

public class Physics {
	
	// gravitational constant
	private static final double defaultG = 6.67428e-11;
	
	// gravitational constant in sim
	public static double G = defaultG;
	
	// one AU in meters
	public static final double AU = 149.59e9;

	// scale is AU in meters, divided by a zoom amount
	// when something is being multiplied by scaleAU, the object is to-scale
	// when divided by scaleAU, the object is on the digital-scale grid in 3d
	public static double scaleAU = AU;
	
	// time period that each simulation refresh represents, in seconds
	public static final double timeScale = 3600 * 24; // default 24*3600 (seconds/day)
	
	public static void resetG() {
		G = defaultG;
	}
	
	public static void scaleG(double scale)	{
		G *= scale;
	}
	
}

