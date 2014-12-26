package screensaver;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Dictionary;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.colorchooser.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ColorChooserUI;

/**
 * The canvas where the screen saver is drawn.
 */
public class Canvas extends JComponent {

	// A list of nodes. Only one is used in the sample, but there's
	// no reason there couldn't be more.
	private ArrayList<SSNode> nodes = new ArrayList<SSNode>();
	private static int FPS = 60; // How often we update the animation.
	private Timer timer; // The timer to actually cause the animation updates.
	private SSNode selectedNode = null; // Which node is selected; null if none
	private MouseHandler mouseHandler;
	private MouseWheelHandler mouseWheelHandler;
	private SSNode selected;

	public Canvas() {

		/*
		 * The mouse input listener combines MouseListener and
		 * MouseMotionListener. Still need to add it both ways, though.
		 */
		mouseHandler = new MouseHandler();
		mouseWheelHandler = new MouseWheelHandler();
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
		this.addMouseWheelListener(mouseWheelHandler);
		this.setName("Canvas");
		this.setOpaque(true); // we paint every pixel; Java can optimize

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				tick();
			}
		};

		timer = new Timer(1000/FPS, listener);

		timer.start();
	}

	private void tick() {
		if (this.mouseHandler.mouseInWindow())
			return;
		for (SSNode n : nodes) {
			n.tick();
		}
		repaint();
	}

	/**
	 * Paint this component: fill in the background and then draw the nodes
	 * recursively.
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		for (SSNode n : nodes) {
			n.paintNode((Graphics2D) g);
		}
	}

	/**
	 * Add a new node to the canvas.
	 */
	public void addNode(SSNode n) {
		this.nodes.add(n);
	}

	/**
	 * Get the node containing the point p. Return null if no such node exists.
	 */
	public SSNode getNode(Point2D p) {
		SSNode hit = null;
		int i = 0;
		while (hit == null && i < nodes.size()) {
			hit = nodes.get(i).hitNode(p);
			i++;
		}
		return hit;
	}

	/**
	 * Convert p to a Point2D, which has higher precision.
	 */
	private Point2D.Double p2D(Point p) {
		return new Point2D.Double(p.getX(), p.getY());
	}

	class MouseWheelHandler implements MouseWheelListener{
		@Override
		public void mouseWheelMoved(MouseWheelEvent e){
			selectedNode = getNode(p2D(e.getPoint()));
			if (selected != null && selectedNode == selected){
				selected.mouseWheel(e);
				repaint();
			}
		}
		
	}
	
	/**
	 * Listen for mouse events on the Canvas. Pass them on to the selected node
	 * (if there is one) in most cases.
	 */
	class MouseHandler implements MouseInputListener {
		private boolean mouseIn;

		private void menuClickHandler(MouseEvent e){
			String itemName = e.getComponent().getName();
			
			switch (itemName){
			case "Add Child":
				SSNode temp = new SSNode(selected.shape, selected.color);
				selected.addChild(temp);
				break;
			
			case "Delete this and its children":
				selected.getParent().removeChild(selected);
				break;
				
			case "Change Colour":
				Color color = JColorChooser.showDialog( Canvas.this,
                        "Choose a color", Color.black );
				selected.color = color;
				break;
			
			case "Heart":
				selected.changeShape(Main.heart);
				break;
			case "Star":
				selected.changeShape(Main.fiveStar);
				break;
			case "Plus":
				selected.changeShape(Main.plus);
				break;
			case "Rectangle":
				selected.changeShape(Main.rectangle);
				break;
				
				
			default:
				break;
			}
			repaint();
		}
	
		
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			selectedNode = getNode(p2D(e.getPoint()));
			String className = e.getComponent().getClass().getName();
			if (className == "javax.swing.JMenuItem"||
				className == "screensaver.Canvas$MouseHandler$ShapeMenu"||
				className == "javax.swing.JPopupMenu"){
				menuClickHandler(e);
				return;
			}

			if (selectedNode != null) {
				selectedNode.mousePressed(e);
				if (selected != null){
					selected.deselect();
				}
					selected = selectedNode;
					selected.select();
				repaint();
			}
			else{
				if (selected != null){
					selected.deselect();
					selected = null;
					repaint();
				}
			}
			
			if (e.getButton() == MouseEvent.BUTTON3 && selected == selectedNode && selectedNode != null){
				SelectedMenu menu = new SelectedMenu(selectedNode.isRoot());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (selectedNode != null) {
				selectedNode.mouseReleased(e);
				repaint();
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseIn = true;

		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseIn = false;

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
			if (selectedNode != null && e.getModifiers() != 4) {
				selectedNode.mouseDragged(e);
				repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
		
		public boolean mouseInWindow(){
			return mouseIn;
		}
		
		class SelectedMenu extends JPopupMenu{
			JMenuItem menuItem;
			ArrayList<String> colours;
			
			public SelectedMenu(boolean isRoot){
				
		        menuItem = new JMenuItem("Add Child");
		        menuItem.setName("Add Child");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        if (!isRoot){
			        menuItem = new JMenuItem("Delete this and its children");
			        menuItem.setName("Delete this and its children");
			        menuItem.addMouseListener(mouseHandler);
			        add(menuItem);
		        }
			    menuItem = new JMenuItem("Change Colour");
		        menuItem.setName("Change Colour");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        menuItem = new ShapeMenu("Change Shape");
		        menuItem.setName("Change Shape");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        
		        
		        this.addMouseListener(mouseHandler);
		        this.setName("Menu");		        
			}		
		}
		
		class ShapeMenu extends JMenu{
			
			public ShapeMenu(String text){
				super(text);
			    JMenuItem menuItem = new JMenuItem("Heart");
		        menuItem.setName("Heart");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        menuItem = new JMenuItem("Star");
		        menuItem.setName("Star");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        menuItem = new JMenuItem("Plus");
		        menuItem.setName("Plus");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
		        menuItem = new JMenuItem("Rectangle");
		        menuItem.setName("Rectangle");
		        menuItem.addMouseListener(mouseHandler);
		        add(menuItem);
				
			}
		}
	}
	
	
}