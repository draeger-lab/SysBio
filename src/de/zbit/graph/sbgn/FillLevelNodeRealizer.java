package de.zbit.graph.sbgn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.Map;

import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;

public class FillLevelNodeRealizer extends NodeRealizer {
	
	private double percent;
	private int[] angles;

	public FillLevelNodeRealizer(NodeRealizer arg0, double percent, int[] angles) {
		super(arg0);
		this.percent = percent;
		this.angles = angles;
	}

	@Override
	public NodeRealizer createCopy(NodeRealizer arg0) {
		return null;
	}

	@Override
	protected void paintNode(Graphics2D g) {
		// draws node with filling color
		g.setColor(Color.red); 
		g.fillOval((int)getX(),(int)getY(),(int)getWidth(),(int)getHeight()); 
		
		if(getPercent() != -1) {								
			int[] angles = getAngles();
		
			// draws white not filled part
			g.setColor(Color.white); 	
			Arc2D arc = new Arc2D.Double(getX()+1, getY()+1, getWidth()-3, getHeight()-3,
					angles[0],angles[1], Arc2D.CHORD);
			g.fill(arc);	
			
		} else {
			System.out.println("NodeRealizer in ManipulatorOfFillLevel: \n "
					+ "I do not have a correct concentration value to display");	
		}

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
	
	public void resetAllNodeRealizers(Graph2D graph, Map<Node,NodeRealizer> node2Realizer) {
		
		for(Map.Entry<Node, NodeRealizer> entry : node2Realizer.entrySet()) {
		    graph.setRealizer(entry.getKey(), entry.getValue());
			System.out.println("Node: " +entry.getKey()+" und Realizer: "+entry.getValue());
		}
	}
}
