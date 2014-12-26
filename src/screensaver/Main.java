package screensaver;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

/**
 * The main class to run the program.
 */
public class Main {

	public static final Shape star = new Polygon(new int[] { 0, 4, 20, 4, 0,
			-4, -20, -4, 0 }, new int[] { -20, -4, 0, 4, 40, 4, 0, -4, -20 }, 9);
	public static final GeneralPath fiveStar = Main.makeStar();
	public static final GeneralPath heart = Main.makeHeart();
	public static final GeneralPath plus = Main.makePlus();
	public static final Rectangle2D rectangle = new Rectangle2D.Double(-10, -10, 20, 20);



	public static void main(String[] args) {
		Canvas canvas = new Canvas();

		// Create the nodes that will be part of the screen saver.
		SSNode root = new SSNode(fiveStar, Color.RED);
		SSNode child0 = new SSNode(heart, Color.RED);
		SSNode child1 = new SSNode(heart, Color.RED);
		SSNode child0_0 = new SSNode(heart, Color.RED);
		SSNode child0_1 = new SSNode(heart, Color.BLUE);
		root.translate(500 / 2, 500 / 2);

		root.addChild(child0);
		child0.translate(150,0);
		child0.rotate(Math.PI/2);
		
		
		root.addChild(child1);
		child1.translate(0,200);
		child1.rotate(Math.PI);
		
		
		
		child0.addChild(child0_0);
		child0_0.translate(75, 0);
		child0_0.rotate(Math.PI / 2);
		child0_0.scale(0.5, 0.5);
		
		child0.addChild(child0_1);
		child0_1.translate(-75, 0);
		child0_1.scale(0.5, 0.5);
		child0_1.rotate(-Math.PI / 2);
		// Make a ring of 6 nodes around child_0_0.
		for(int i=0; i<6; i++) {
			double d = 60;
			SSNode c = new SSNode(heart, Color.GREEN);
			double rot = i*Math.PI/6;
			child0_0.addChild(c);
			c.rotate(rot);
			c.translate(d*Math.cos(rot), d*Math.sin(rot));
			c.scale(0.75, 0.75);
			
		}

		// Make the scene graph.
		/*
		
		*/

		canvas.addNode(root);

		JFrame frame = new JFrame("Screensaver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 522);
		frame.setContentPane(canvas);
		frame.setVisible(true);
		

		
		
	}

	/**
	 * Make a heart-shaped shape at the origin.
	 */
	private static GeneralPath makeHeart() {
		GeneralPath heart = new GeneralPath();
		heart.moveTo(0, 0);
		heart.curveTo(0, -5, 5, -10, 10, -10);
		heart.curveTo(15, -10, 20, -5, 20, 0);
		heart.curveTo(20, 10, 5, 25, 0, 30);
		heart.curveTo(-5, 25, -20, 10, -20, 0);
		heart.curveTo(-20, -5, -15, -10, -10, -10);
		heart.curveTo(-5, -10, 0, -5, 0, 0);
		return heart;
	}
	
	private static GeneralPath makeStar() {
		GeneralPath star = new GeneralPath();
		star.moveTo(0,-24);
		star.lineTo(5, -7);
		star.lineTo(24, -6);
		star.lineTo(9, 5);
		star.lineTo(15, 23);
		star.lineTo(0, 13);
		star.lineTo(-15, 23);
		star.lineTo(-9, 5);
		star.lineTo(-24, -6);
		star.lineTo(-5,-7);
		star.lineTo(0, -24);
		return star;
	}
	
	private static GeneralPath makePlus() {
		GeneralPath plus = new GeneralPath();
		plus.moveTo(-5,-25);
		plus.lineTo(5, -25);
		plus.lineTo(5, -5);
		plus.lineTo(25, -5);
		plus.lineTo(25, 5);
		plus.lineTo(5, 5);
		plus.lineTo(5, 25);
		plus.lineTo(-5, 25);
		plus.lineTo(-5, 5);
		plus.lineTo(-25,5);
		plus.lineTo(-25, -5);
		plus.lineTo(-5, -5);
		plus.lineTo(-5, -25);
		return plus;
	}
	
}
