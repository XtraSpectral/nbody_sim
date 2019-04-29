package main.simulation;

import main.objects.Poly;
import main.objects.Node;
import main.objects.PhysicsNode;
import main.objects.Transit;
import main.objects.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import java.util.concurrent.ThreadLocalRandom;


public class Galaxy extends ArrayList<PhysicsNode> {

	/**The Galaxy class is responsible for maintaining + organizing all
	 * objects contained within the simulation.
	 */
	
	private static final long serialVersionUID = 1L;
	private int seed;
	private int activeKey = -1;
	private int galaxyRadius = 15;

	// hashmap of nodes to a list of transits that originate from that node
	private Map<Node, List<Transit>> transits = new HashMap<Node, List<Transit>>();
	private List<PhysicsNode> energySources = new ArrayList<PhysicsNode>();
	private Vector selectionTransit;	  
	private boolean refreshTransits = true;
    
	
	/* ========================================== */
	/* ===== CONSTRUCTORS AND CLASS OPTIONS ===== */
	/* ========================================== */

	public Galaxy(int seed) {
		newSeed(seed);
	}	

	public void reset() {
		resetActive();
		clear();
		newSeed(seed);
	}
	
	private Point3D makePosition() {
		// place randomly within a cylindrical space 
		double t = Math.toRadians(2 * Math.PI * ThreadLocalRandom.current().nextInt(0, 360));
		double r = ThreadLocalRandom.current().nextInt(2, galaxyRadius);
		double x = r * Math.cos(t);
		double y = r * Math.sin(t);
		double z = ThreadLocalRandom.current().nextDouble(-2.00, 2.01);
		return new Point3D(x, y, z);
	}
	
	private void newSeed(int seed) {	
		// create an initial energy source
		PhysicsNode sun = new PhysicsNode(new Point3D(0, 0, 0), 0, "star", 8000, .1, 695700, 1.989e30);
		energySources.add(sun);
		add(sun);
		
		// procedurally create objects, providing a random position and an id number value
		for (int i=0; i<seed; i++) {
			PhysicsNode obj = new PhysicsNode(this.makePosition(), this.size());
			add(obj);
		}
		
		this.seed = size(); // since there is a minimum number of objects even if seed is zero
		System.out.printf("Galaxy() %d object seed complete%n", seed);
	}
	
	/* =================================== */
	/* ===== SELECTED/ACTIVE OBJECTS ===== */
	/* =================================== */

	public void setActive(int index) {
		// reset active key if same as new active key
		if (index == activeKey || index == -1) {
			if (getActive() != null) {
				getActive().deselect();
			}
			activeKey = -1; 
		} else if (index < this.size() && index >= 0) {
			if (hasActive()) {
				getActive().deselect();
			}

			// set new activeKey and select newly active object
			activeKey = index;
			getActive().select();
		} else {System.out.println("Galaxy.setActive bad argument received");}
	}
	
	public void drawActive(Vector camera, GraphicsContext gc) {
		
		// do not do anything if no active object or not renderable
		if (!hasActive()) {return;}
		if (!getActive().isRenderable()) {return;}
		
		// create a new transit animation line from selection to central object
		// check how it compares with the previous line, to determine
		// animation settings (doppler shift indicator, dashed line animation setting)
		Vector v = new Vector(getActive().getXYZ(), get(0).getXYZ());
		if (selectionTransit != null) {
			v.setAnimationModifier(selectionTransit.getAnimationModifier());
			v.setOldDistance(selectionTransit.getDistance());
		}
		
		// set transit animation line for current frame, then draw it
		this.selectionTransit = v;
		v.drawVectorAnimation(camera, gc);
		
		// draw labels on selected objects
		getActive().drawSelectionVisuals(gc, camera);
	}
	
	public boolean hasActive() {
		return activeKey < this.size() && activeKey >= 0 && get(activeKey) != null;
	}
	
	public PhysicsNode getActive() {return hasActive() ? get(activeKey) : null;}
	
	public void resetActive() {setActive(-1);}
	
	
	
	/* ============================== */
	/* ===== MANIPULATE OBJECTS ===== */
	/* ============================== */
	
	public void updateAllObjects(boolean isPaused) {
		
		// generate centers of mass
    	stream().forEach(x -> x.getXYZ());

		// if physics are not paused...
		if (!isPaused) {
			// apply physical and energy forces
	    	stream().forEach(x -> x.applyForcesFrom(this));
		}
		
		// FIXME still do this if isPaused, because space can be contracted, affecting energy
		for (int i=0; i<size(); i++) {
			// update object coloring
			for (PhysicsNode energysource : getEnergySources()) {
				get(i).applyTempFrom(energysource);
			}
			if (!isPaused) {
				// apply updates to object positioning
				get(i).applyAllForces();			
			}
		}
	}
	
	public List<Poly> getPolygons(Vector camera) {
		List<Poly> allPolygons = new ArrayList<Poly>();
		for (int i=0; i<size(); i++) {
			// TODO temporarily disable rendering objects that contain the camera 
			//	if (get(i).containsPoint3D(camera.getViewFrom())) {get(i).turnOffThisRenderCycle();}
			
			// update and draw if renderable
			if (get(i).isRenderable()) {
				for (Poly p : get(i).getPolygons()) {					
					p.update(camera);
					allPolygons.add(p);
				}
			}
		}
		// order all polygons
		try {
			allPolygons.sort((o1, o2) -> o1.compareTo(o2));			
		} catch (IllegalArgumentException e) {
			// catches if avgdist is NaN
			allPolygons.stream().forEach(x -> System.out.println(x.getDistance()));
			System.out.println("Galaxy.getPolygons illegalargumentexception caught");
		}
		return allPolygons;
	}

	
    public void scaleVelocity(double scale) {
    	stream().forEach(x -> x.velocityMultiplier(scale));
    }
    
	public void scaleSpace(double scale) {
    	stream().forEach(x -> x.expandSpaceAroundShape(scale));
	}
	
	public void makeStar() {
		if (hasActive() && !getActive().getType().equals("star")) {
			getActive().setType("star");
			energySources.add(getActive());
		}
	}
	
	public List<PhysicsNode> getEnergySources() {
		return energySources;
	}
	
	public void scaleEnergy(double scale) {
		getEnergySources().stream().forEach(x -> x.scaleKelvin(scale));
	}
	
	public void resetEnergy() {
		getEnergySources().stream().forEach(x -> x.resetKelvin());
	}
	
	
	
	/* ===================================== */
	/* ===== TRANSIT RULES AND DRAWING ===== */
	/* ===================================== */
	
    private void newTransits() {
    	transits.clear();
    	
    	for (PhysicsNode p1 : this) {
    		  		
    		double dist1 = 5.0;
    		double dist2 = 5.0;
    		double dist3 = 5.0;

    		PhysicsNode closest1 = null;
    		PhysicsNode closest2 = null;
    		PhysicsNode closest3 = null;
    		
        	for (PhysicsNode p2 : this) {
        		if (p1.equals(p2)) {continue;}

        		double newDist = p1.getXYZ().distance(p2.getXYZ());       		
        		
        		if (newDist <= dist1 && dist1 > dist2 && dist1 > dist3) {
        			closest1 = p2;
        			dist1 = newDist;
        		} else {
	        		if (newDist <= dist2 && dist2 > dist3) {
	        			closest2 = p2;
	        			dist2 = newDist;
	        		} else if (newDist <= dist3) {
	        			closest3 = p2;
	        			dist3 = newDist;
	        		}
        		}
        	}
        	
        	List<Transit> newTransits = new ArrayList<Transit>();
        	if (closest1!=null) {
	        	Transit t1 = new Transit(p1, closest1);
	        	newTransits.add(t1);
        	}
        	if (closest2!=null) {
	        	Transit t2 = new Transit(p1, closest2);
	        	newTransits.add(t2);
        	}
        	if (closest3!=null) {
	        	Transit t3 = new Transit(p1, closest3);
	        	newTransits.add(t3);  
        	}
        	// add physicsobject and transits to master transits map
            transits.put(p1, newTransits);
    	}
    }
    
    public void drawAllTransits(GraphicsContext g, Vector camera) {
    	// creates new transits only every other refresh, for performance
    	if (refreshTransits) {
    		newTransits();  
    	}
		refreshTransits = !refreshTransits;

		g.setLineWidth(1);

		for (int i=0; i<size(); i++) {
			for (Transit t : transits.get(get(i))) {
	    		if (t.getDistance()<5.0) {
	    			// sources and destinations must extend class Node
		    		Point2D source = camera.toScreenSpace(t.getSource().getXYZ());
		    		Point2D dest = camera.toScreenSpace(t.getDestination().getXYZ());
		    		g.setStroke(((Node) t.getSource()).getColor());
		    		g.strokeLine(source.getX(), source.getY(), dest.getX(), dest.getY());
	    		}
	    	}
		}		
    }
}
