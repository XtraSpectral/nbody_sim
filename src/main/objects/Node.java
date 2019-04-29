package main.objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Node {
	
	/**Positional, shape, and id information for simulation objects.
	 * May be kept as static or extended to implement other features,
	 * like physics.
	 */

	private Color color; // rgb color
	private Point3D xyz; // position of object in simulation space
	private int id; // identification number
	private Point3D lwh; // length, width, height of object
	private boolean renderable = true; // will be drawn if true
	private List<Poly> polygons = new ArrayList<Poly>(); // associated polygons
	
	public Node(int l, int w, int h, int id) {
		this(new Point3D(0-l/2, 0-w/2, 0-h/2), id, 0);
		renderable = false; // WARNING turned off by default if rendered as plane
		asPlane(l, w, h);
	}
	
	public Node(Point3D xyz, int id, double size) {
		this(xyz, id, 0, ThreadLocalRandom.current().nextInt(6000, 60000), size);
	}
	
	public Node(Point3D xyz, int id, int kelvin, int radius, double size) {
		this.xyz = xyz;
		this.id = id;		
		this.lwh = new Point3D(size, size, size);
	}

	public void asCube() {	
		
		double x = this.xyz.getX() - this.lwh.getX();
		double y = this.xyz.getY() - this.lwh.getY();
		double z = this.xyz.getZ() - this.lwh.getZ();
		
		double l = this.lwh.getX();
		double w = this.lwh.getY();
		double h = this.lwh.getZ();
		
		int id = getID();
		
		polygons.add(new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y+w, y+w}, 
				new double[]{z, z, z, z}, id));
		polygons.add(new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y+w, y+w},  
				new double[]{z+h, z+h, z+h, z+h}, id));
		polygons.add(new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y, y},  
				new double[]{z, z, z+h, z+h}, id));
		polygons.add(new Poly(new double[]{x, x+l, x+l, x}, new double[]{y+w, y+w, y+w, y+w},  
				new double[]{z, z, z+h, z+h}, id));
		polygons.add(new Poly(new double[]{x, x, x, x}, new double[]{y, y+w, y+w, y},  
				new double[]{z, z, z+h, z+h}, id));
		polygons.add(new Poly(new double[]{x+l, x+l, x+l, x+l}, new double[]{y, y+w, y+w, y},  
				new double[]{z, z, z+h, z+h}, id));
	}

	public void asPlane(int l, int w, int h) {		
		
		int dim1Size = l == 0 ? w : l;
		int dim2Size = h == 0 ? w : h;
		double[] emptyDim = new double[] {0, 0, 0, 0};
		
		for (int i=-dim1Size/2;i<dim1Size/2;i++) {
			for (int j=-dim2Size/2;j<dim2Size/2;j++) {
				
				double[] dim1Points = l == 0 ? emptyDim : new double[]{i, i, i+1, i+1};
				double[] dim2Points = w == 0 ? emptyDim : new double[]{j, j+1, j+1, j};
				double[] dim3Points = h == 0 ? emptyDim : new double[]{j, j+1, j+1, j};
				dim3Points = l == 0 ? new double[]{i, i, i+1, i+1} : dim3Points;

				Poly cell = new Poly(dim1Points,dim2Points,dim3Points,getID());
				cell.setColor(Color.DARKGRAY);
				polygons.add(cell);
			}
		}
	}

    public Color getColor() {
    	return color;
    }
    
	public void setColor(Color color) {
		// change color of this shape and all of its polygons
		polygons.stream().forEach(p -> p.setColor(color));
		this.color = color;
	}
	
	public void toggleRenderable() {
		renderable = !renderable;
	}
	
	public boolean isRenderable() {
		return renderable;
	}
	
	public List<Poly> getPolygons() {
		return polygons;
	}
	
	public void select() {
		// select all polygons
		polygons.stream().forEach(p -> p.toggleSelect(true));
	}
	
	public void deselect() {
		// deselect all polygons
		polygons.stream().forEach(p -> p.toggleSelect(false));
	}
	
	public void drawSelectionVisuals(GraphicsContext g, Vector v) {
		Point2D xy = v.toScreenSpace(this.xyz);
		g.setLineWidth(1);
		g.setStroke(Color.ORANGE);
		g.strokeLine(xy.getX(), xy.getY(), xy.getX()+10, xy.getY()-10);
		g.strokeText(toString(), xy.getX()+12, xy.getY()-11);
		String positionText = String.format("%.2f:%.2f:%.2f", this.xyz.getX(), this.xyz.getY(), this.xyz.getZ());
		g.strokeText(positionText, xy.getX()+12, xy.getY());
	}

	public Point3D getXYZ() {
		return xyz;
	}
	
	public void setXYZ(Point3D xyz) {
		this.xyz = xyz;
	}

	public int getID() {
		return id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + toString().hashCode();
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
        Node other = (Node) obj;
        if (toString() == null) {
            if (other.toString() != null)
                return false;
        } else if (!toString().equals(other.toString()))
            return false;
        return true;
    }

}
