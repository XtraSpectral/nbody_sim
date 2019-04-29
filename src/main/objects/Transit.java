package main.objects;

import javafx.scene.paint.Color;

public class Transit {
	
	/**Data structure resembling a directed graph edge connecting
	 * two vertices (nodes)
	 */
	
	private String name;
    private final PhysicsNode v1;
    private final PhysicsNode v2;
    private double distance;
    private Color color = null;

    public Transit(PhysicsNode source, PhysicsNode destination) {
        this.v1 = source;
        this.v2 = destination;
        this.name = Integer.toString(v1.getID()) + Integer.toString(v2.getID());
		this.distance = v1.getXYZ().distance(v2.getXYZ());
    }
    
    public PhysicsNode getDestination() {
        return v2;
    }

    public PhysicsNode getSource() {
        return v1;
    }
    public double getDistance() {
        return distance;
    }
    
    public String getName() {
    	return name;
    }
    
    public boolean hasColor() {
    	return color == null;
    }
    
    public Color getColor() {
    	return color;
    }
    
    public void setColor(Color c) {
    	color = c;
    }
    
    @Override
    public String toString() {
        return v1 + " " + v2;
    }
    
}
