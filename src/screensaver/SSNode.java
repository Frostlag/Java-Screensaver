package screensaver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;
/**
 * Represent one Screen Save Node.
 */
public class SSNode implements MouseMotionListener, MouseListener {

	private AffineTransform trans = new AffineTransform();
	public Shape shape;
	private ArrayList<SSNode> children = new ArrayList<SSNode>();
	private SSNode parent = null;
	private String id; // for debugging printf statements
	private Point2D lastPoint = null;
	public Color color = Color.RED;
	private double xToParent;
	private double yToParent;
	private double angle;
	private double xScale;
	private double yScale;
	private double rotSpeed = Math.PI/120;
	private double scaleSpeed = 1.1;
	
	private boolean outline;

	
	public void rotate(double angle, double x, double y){
		
		this.transform(AffineTransform.getRotateInstance(angle,x,y));
		this.angle += angle;
		//System.out.print(this.angle +  "\n");
	}
	
	public void rotate(double angle){
 
		this.angle += angle;
		Point2D parent = new Point2D.Double(xToParent, yToParent); 
		parent = AffineTransform.getRotateInstance(-angle).transform(parent, null);
		this.xToParent = parent.getX();
		this.yToParent = parent.getY();
		this.transform(AffineTransform.getRotateInstance(angle));
		//System.out.print(xToParent + " " + yToParent + "\n");
		//System.out.print(angleToParent + "\n");
	}
	
	public void translate(double x, double y){
		
		this.xToParent -= x;
		this.yToParent -= y;
		//System.out.print( tempAngle + "\n");
		
		this.transform(AffineTransform.getTranslateInstance(x,y));
	}
	
	public void scale(double x, double y){
		xScale *= x;
		yScale *= y;
		xToParent *= 1/x;
		yToParent *= 1/y;
		this.transform(AffineTransform.getScaleInstance(x,y));
		
	}
		
	/**
	 * Create a new SSNode, given a shape and a colour.
	 */
	public SSNode(Shape s, Color color) {
		this.id = "id";
		this.shape = s;
		this.color = color;
		angle = 0;
		xScale = 1;
		yScale = 1;
	}

	/**
	 * Set this node's shape to a new shape.
	 */
	public void setShape(Shape s) {
		this.shape = s;
	}

	/**
	 * Add a child node to this node.
	 */
	public void addChild(SSNode child) {
		child.id = this.id + "." + (this.children.size());
		this.children.add(child);
		child.parent = this;
		child.xToParent = 0;
		child.yToParent = 0;
	}

	/**
	 * Is this node the root node? The root node doesn't have a parent.
	 */
	public boolean isRoot() {
		return this.parent == null;
	}

	/**
	 * Get this node's parent node; null if there is no such parent.
	 */
	public SSNode getParent() {
		return this.parent;
	}

	/**
	 * One tick of the animation timer. What should this node do when a unit of
	 * time has passed?
	 */
	public void tick() {
		if (!isRoot()){
			if (this.getParent() != null && !this.outline){
				this.rotate(rotSpeed,xToParent,yToParent);
				//System.out.println(this.getFullInverseTransform());
			}
		}
		for (SSNode n : this.children){
			n.tick();		
		}
	}
	
	
	/**
	 * Does this node contain the given point (which is in window coordinates)?
	 */
	public boolean containsPoint(Point2D p) {
		AffineTransform inverseTransform = this.getFullInverseTransform();
		Point2D pPrime = inverseTransform.transform(p, null);
			
		return this.shape.contains(pPrime);
	}

	/**
	 * Return the node containing the point. If nodes overlap, child nodes take
	 * precedence over parent nodes.
	 */
	public SSNode hitNode(Point2D p) {
		for (SSNode c : this.children) {
			SSNode hit = c.hitNode(p);
			if (hit != null)
				return hit;
		}
		if (this.containsPoint(p) && !this.outline) {
			return this;
		} else {
			return null;
		}
	}

	/**
	 * Transform this node's transformation matrix by concatenating t to it.
	 */
	public void transform(AffineTransform t) {
		this.trans.concatenate(t);
	}

	/**
	 * Convert p to a Point2D.
	 */
	private Point2D.Double p2D(Point p) {
		return new Point2D.Double(p.getX(), p.getY());
	}

	/*************************************************************
	 * 
	 * Handle mouse events directed to this node.
	 * 
	 *************************************************************/

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.lastPoint = p2D(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.lastPoint = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

    /**
     * Handle mouse drag event, with the assumption that we have already
     * been "selected" as the sprite to interact with.
     * This is a very simple method that only works because we
     * assume that the coordinate system has not been modified
     * by scales or rotations. You will need to modify this method
     * appropriately so it can handle arbitrary transformations.
     */
	@Override
	public void mouseDragged(MouseEvent e) {
		try{
			Point2D mouse = this.getFullTransform().inverseTransform(p2D(e.getPoint()),null);
			
			double oldAngle = Math.atan2(-this.xToParent,-this.yToParent);

			this.translate(mouse.getX(),mouse.getY());
			
			double newAngle = Math.atan2(-this.xToParent,-this.yToParent);
			if (!this.isRoot()){
				this.rotate(oldAngle - newAngle);
			}
			//System.out.println(oldAngle - newAngle);
			
			lastPoint = mouse;
		}
		catch (Exception ex){}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Paint this node and its children.
	 */
	public void paintNode(Graphics2D g2) {
		/*
		 * You can change this code if you wish. Based on an in-class example
		 * it's going to be really tempting. You are advised, however, not to
		 * change it. Doing so will likely bring you hours of grief and much
		 * frustration.
		 */

		// Remember the transform being used when called
		AffineTransform t = g2.getTransform();

		g2.transform(this.getFullTransform());
		g2.setColor(this.color);
		if (!this.outline){
			g2.fill(this.shape);
		}
		else{
			g2.setStroke(new BasicStroke(3));
			g2.draw(this.shape);
		}
		// Restore the transform.
		g2.setTransform(t);

		// Paint each child
		for (SSNode c : this.children) {
			c.paintNode(g2);
		}
		// Restore the transform.
		g2.setTransform(t);
	}

	/*
	 * There are a number of ways in which the handling of the transforms could
	 * be optimized. That said, don't bother. It's not the point of the
	 * assignment.
	 */

	/**
	 * Returns our local transform. Copy it just to make sure it doesn't get
	 * messed up.
	 */
	public AffineTransform getLocalTransform() {
		return new AffineTransform(this.trans);
	}

	/**
	 * Returns the full transform to this node from the root.
	 */
	public AffineTransform getFullTransform() {
		// Start with an identity matrix. Concatenate on the left
		// every local transformation matrix from here to the root.
		AffineTransform at = new AffineTransform();
		SSNode curNode = this;
		while (curNode != null) {
			at.preConcatenate(curNode.getLocalTransform());
			curNode = curNode.getParent();
		}
		return at;
	}

	/**
	 * Return the full inverse transform, starting with the root. That is, get
	 * the full transform from here to the root and then invert it, catching
	 * exceptions (there shouldn't be any).
	 */
	private AffineTransform getFullInverseTransform() {
		try {
			AffineTransform t = this.getFullTransform();
			AffineTransform tp = t.createInverse();
			return tp;
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new AffineTransform();
		}
	}
	
	public void select(){
		SSNode outlineShape = new SSNode(this.shape, Color.WHITE);
		this.addChild(outlineShape);
		outlineShape.scale(1.1, 1.1);
		outlineShape.outline = true;
		//System.out.println(outlineShape);
	}
	
	public void deselect(){
		for (SSNode n : this.children){
			if (n.outline){
				this.children.remove(n);
				return;
			}
		}
	}
	
	public void removeChild(SSNode child){
		for (SSNode n : this.children){
			if (n == child){
				this.children.remove(n);
				return;
			}
		}
	}
	
	public void mouseWheel(MouseWheelEvent e){
		if (e.getWheelRotation() < 0){
			this.scale(scaleSpeed,scaleSpeed);
			for (SSNode n : this.children){
				if (!n.outline){
					n.scale(1/scaleSpeed,1/scaleSpeed);
					n.translate(n.xToParent*(-1/scaleSpeed+1),n.yToParent*(-1/scaleSpeed+1));
				}
			}
		}
		else if (e.getWheelRotation() > 0){
			this.scale(1/scaleSpeed,1/scaleSpeed);
			for (SSNode n : this.children){
				if (!n.outline){
					n.scale(scaleSpeed,scaleSpeed);
					n.translate(-n.xToParent*(scaleSpeed-1),-n.yToParent*(scaleSpeed-1));
				}
			}
		}
	}
	public void changeShape(Shape s){
		this.shape = s;
		for (SSNode n : this.children){
			if (n.outline){
				n.shape = s;
			}
		}
	}

}
