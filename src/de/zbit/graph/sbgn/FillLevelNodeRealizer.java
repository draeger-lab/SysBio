package de.zbit.graph.sbgn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.HashMap;
import java.util.Map;

import y.base.Node;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * An extension of {@link ShapeNodeRealizer} supporting a
 * {@link CloneMarker}.
 * 
 * @author Lea Buchweitz
 */

public class FillLevelNodeRealizer extends ShapeNodeRealizer implements SimpleCloneMarker {
	
	/**
	   * Is this node a cloned node (i.e., another
	   * instance must exist in the same graph)?
	   */
	private boolean isClonedNode = false;
	
	private double percent;
	private int[] angles;
	private Color color;
	public static final Map<Node,FillLevelNodeRealizer> node2FillRealizer = new HashMap<Node,FillLevelNodeRealizer>();

	//public FillLevelNodeRealizer(NodeRealizer arg0, double percent, int[] angles, Color color, NodeLabel label) {
	public FillLevelNodeRealizer(NodeRealizer arg0) {
		
		super(arg0);
		//this.percent = percent;
		//this.angles = angles;
		//this.color = color;
	}

	@Override
	public NodeRealizer createCopy(NodeRealizer arg0) {
		return null;
	}

	@Override
	protected void paintNode(Graphics2D g) {
		// draws node with filling color
		g.setColor(getColor()); 
		g.fillOval((int)getX(),(int)getY(),(int)getWidth(),(int)getHeight()); 
		
		if(getPercent() != -1) {								
			int[] angles = getAngles();
		
			// draws white not filled part
			g.setPaint(Color.white);
			Arc2D arc = new Arc2D.Double(getX()+1, getY()+1, getWidth()-3, getHeight()-3,
					angles[0],angles[1], Arc2D.CHORD);
			g.fill(arc);	
			
		} else {
			System.out.println("NodeRealizer in ManipulatorOfFillLevel: \n "
					+ "I do not have a correct concentration value to display");	
		}

	}
	
	@Override
	public void paintSloppy(Graphics2D g) {
		paintNode(g);
	}
	
	public int[] getAngles() {
		return angles;
	}
	
	public void setAngles(int[] angles) {
		this.angles = angles;
	}
	
	public double getPercent() {
		return this.percent;
	}
	
	public void setPercent(double percent) {
		this.percent = percent;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setNodeIsCloned(boolean b) {
		isClonedNode = b;
	}

	@Override
	public boolean isNodeCloned() {
		return isClonedNode;
	}
	
}
