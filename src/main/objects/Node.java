package main.objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Node {
	

	private Color color;
	private Color frameColor; // TODO remove this - its in polygons

	private Point3D xyz;
	private Point3D defaultPos;
	private int id;
	
	// shape details
	private Point3D lwh;
	private boolean renderable = true;
	private List<Poly> polygons = new ArrayList<Poly>();
	
	public Node(int l, int w, int h, int id) {
		this(new Point3D(0-l/2, 0-w/2, 0-h/2), id);
		renderable = false; // WARNING turned off by default
		renderAsPlane(l, w, h);
		configurePolygons();
	}


	public Node(Point3D xyz, int id) {
		this(xyz, id, "planet", 0, ThreadLocalRandom.current().nextInt(6000, 60000), .1);
	}
	
	public Node(Point3D xyz, int id, double size) {
		this(xyz, id, "planet", 0, ThreadLocalRandom.current().nextInt(6000, 60000), size);
	}
	
	public Node(Point3D xyz, int id, String type, int kelvin, int radius, double size) {
		this.xyz = xyz;
		this.defaultPos = this.xyz;
		this.id = id;		
		this.lwh = new Point3D(size, size, size);
	}
	
	private void configurePolygons() {
		// creates a default color for polygons
		// can also set outline and fill settings
		for (int i=0; i<polygons.size(); i++) {
			polygons.get(i).setColor(Color.DARKGRAY);
			polygons.get(i).setDrawFrameOnly(true);
		}	
	}
	
	public void renderAsCube() {	
		
		double x = this.xyz.getX() - this.lwh.getX();
		double y = this.xyz.getY() - this.lwh.getY();
		double z = this.xyz.getZ() - this.lwh.getZ();
		
		double l = this.lwh.getX();
		double w = this.lwh.getY();
		double h = this.lwh.getZ();
		
		int id = getID();
		
		Poly bottom = new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y+w, y+w}, 
				new double[]{z, z, z, z}, id);
		Poly top = new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y+w, y+w},  
				new double[]{z+h, z+h, z+h, z+h}, id);
		Poly bRight = new Poly(new double[]{x, x+l, x+l, x}, new double[]{y, y, y, y},  
				new double[]{z, z, z+h, z+h}, id);
		Poly fLeft = new Poly(new double[]{x, x+l, x+l, x}, new double[]{y+w, y+w, y+w, y+w},  
				new double[]{z, z, z+h, z+h}, id);
		Poly bLeft = new Poly(new double[]{x, x, x, x}, new double[]{y, y+w, y+w, y},  
				new double[]{z, z, z+h, z+h}, id);
		Poly fRight = new Poly(new double[]{x+l, x+l, x+l, x+l}, new double[]{y, y+w, y+w, y},  
				new double[]{z, z, z+h, z+h}, id);
		
		polygons.add(bottom);
		polygons.add(top);
		polygons.add(bRight);
		polygons.add(fLeft);
		polygons.add(bLeft);
		polygons.add(fRight);
	}
	
	public void renderAsDisc(int radius, int n) {
		// TODO draw orbits every unit of radius (1AU = 1), n times
	}
	
	public void renderAsPlane(int l, int w, int h) {		
		
		int dim1Size = l == 0 ? w : l;
		int dim2Size = h == 0 ? w : h;
		
		for (int i=-dim1Size/2;i<dim1Size/2;i++) {
			for (int j=-dim2Size/2;j<dim2Size/2;j++) {
				
				
				double[] dim1Points = l == 0 ? new double[]{0,0,0,0} : new double[]{i, i, i+1, i+1};
				double[] dim2Points = w == 0 ? new double[]{0,0,0,0} : new double[]{j, j+1, j+1, j};
				double[] dim3Points = h == 0 ? new double[]{0,0,0,0} : new double[]{j, j+1, j+1, j};
				dim3Points = l == 0 ? new double[]{i, i, i+1, i+1} : dim3Points;

				
				Poly cell = new Poly(dim1Points,dim2Points,dim3Points,getID());
				cell.setColor(Color.DARKGRAY);
				cell.setDrawFrameOnly(true);
				polygons.add(cell);
			}
		}
	}

	
	public void draw(GraphicsContext g, Camera v)	{
		
		Point2D xy = v.transformPointTo2D(this.xyz);
		PixelWriter pw = g.getPixelWriter();
		pw.setColor((int) xy.getX(), (int) xy.getY(), color);
	}
	


    public Color getColor() {
    	return color;
    }
    
	public void changeColor(Color color) {
		// change color of this shape and all of its polygons
		polygons.stream().forEach(p -> p.setColor(color));
		this.color = color;
	}
    
	public void changeFrameColor(Color color) {
		// change color of this shape and all of its polygons
		polygons.stream().forEach(p -> p.setFrameColor(color));
		this.frameColor = color;
	}
	
	public void toggleRenderable() {
		renderable = !renderable;
	}
	
	public boolean isRenderable() {
		return renderable;
	}
	
	public void setRenderable(boolean render) {
		this.renderable = render;
	}
	
	public List<Poly> getPolygons() {
		return polygons;
	}
	
	public void impromptuDraw(GraphicsContext g, Camera c) {
		// do a quick, quasi-detailed rendering of this node's polygons
		polygons.stream().forEach(p -> p.drawPolygon(g, Color.GREEN));
	}


	
	public void select() {
		// select all polygons
		polygons.stream().forEach(p -> p.toggleSelect(true));
	}
	
	public void deselect() {
		// deselect all polygons
		polygons.stream().forEach(p -> p.toggleSelect(false));

	}
	
	public void drawSelectionVisuals(GraphicsContext g, Camera v) {
		Point2D xy = v.transformPointTo2D(this.xyz);
		g.setLineWidth(1);
		g.setStroke(Color.ORANGE);
		g.strokeLine(xy.getX(), xy.getY(), xy.getX()+10, xy.getY()-10);
		g.strokeText(toString(), xy.getX()+12, xy.getY()-11);
		g.strokeText(String.format("%.2f:%.2f:%.2f", this.xyz.getX(), this.xyz.getY(), this.xyz.getZ()), xy.getX()+12, xy.getY());
	}

	
	public void reset() {
		xyz = defaultPos;
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
	

	
	
	public void expandSpaceAroundShape(double scale) {
		xyz = new Point3D(xyz.getX()*scale, xyz.getY()*scale, xyz.getZ()*scale);
	}
	
	
	public double unitDistanceTo(Node o) {
		Camera v = new Camera(o.xyz, this.xyz);
		return v.getDistance();
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
