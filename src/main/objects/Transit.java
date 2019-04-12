package main.objects;

import javafx.scene.paint.Color;

public class Transit {
	private String name;
    private final PhysicsNode v1;
    private final PhysicsNode v2;
    private double distance;
    private Color color = null;


    public Transit(PhysicsNode source, PhysicsNode destination) {
        this.v1 = source;
        this.v2 = destination;
        setName();
        calculateDistances();
    }


	private void setName() {
        name = Integer.toString(v1.getID()) + Integer.toString(v2.getID());
    }
    
    private void calculateDistances() {
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
    
    @Override
    public int hashCode() {
    	// hashing by destination for HashMap<PhysicsObject, List<Transit>> 
    	// structure, representing transits belonging to each object
        final int prime = 31;
        int result = 1;
        result = prime * result + v2.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transit other = (Transit) obj;
        if (name == null) {
            if (other.getName() != null)
                return false;
        } else if (!name.equals(other.getName()))
            return false;
        return true;
    }
    
    
}
