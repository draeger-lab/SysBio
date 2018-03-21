package de.zbit.graph.sbgn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
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
  
  public FillLevelNodeRealizer(NodeRealizer arg0, double percent, int[] angles, Color color) {
    
    super(arg0);
    this.percent = percent;
    this.angles = angles;
    this.color = color;
  }
  
  public FillLevelNodeRealizer(NodeRealizer nr) {
    super(nr);
  }
  
  @Override
  public NodeRealizer createCopy(NodeRealizer fillNodeRealizer) {
    return new FillLevelNodeRealizer(this);
  }
  
  /*
	@Override
	public void paintFilledShape(Graphics2D g) {
		super.paintFilledShape(g);
		Ellipse2D fillLevelEllipse = new Ellipse2D.Double((int)getX(),(int)getY(),(int)getWidth(),(int)getHeight());
		if (!isTransparent() && (getFillColor() != null)) {
			CloneMarker.Tools.paintLowerBlackIfCloned(g, this, fillLevelEllipse);
		}
	}
   */
  
  @Override
  protected void paintNode(Graphics2D g) {
    // draws node with filling color
    g.setColor(Color.black);
    g.fillOval((int)getX()-1,(int)getY()-1,(int)getWidth()+2,(int)getHeight()+2);
    Ellipse2D blackEllipse = new Ellipse2D.Double((int)getX()-1,(int)getY()-1,(int)getWidth()+2,(int)getHeight()+2);
    //g.fill(blackEllipse);
    //g.setClip(blackEllipse);
    Ellipse2D fillLevelEllipse = new Ellipse2D.Double((int)getX(),(int)getY(),(int)getWidth(),(int)getHeight());
    //g.clip(fillLevelEllipse);
    g.setColor(getColor());
    //g.fill(g.getClip());
    g.fillOval((int)getX(),(int)getY(),(int)getWidth(),(int)getHeight());
    
    Arc2D arc = new Arc2D.Double();
    if(getPercent() != -1) {
      int[] angles = getAngles();
      
      // draws white not filled part
      g.setPaint(Color.white);
      arc = new Arc2D.Double(getX()+1, getY()+1, getWidth()-3, getHeight()-3,
        angles[0],angles[1], Arc2D.CHORD);
      g.fill(arc);
      
    } else {
      System.out.println("NodeRealizer in ManipulatorOfFillLevel: \n "
          + "I do not have a correct concentration value to display");
    }
    paintText(g);
    if (!isTransparent() && (getFillColor() != null)) {
      CloneMarker.Tools.paintRightBlackIfCloned(g, this, fillLevelEllipse);
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
    return percent;
  }
  
  public void setPercent(double percent) {
    this.percent = percent;
  }
  
  public Color getColor() {
    return color;
  }
  
  public void setColor(Color color) {
    this.color = color;
  }
  
  /**
   * returns the size of the noderealizer for the camera animation to reset the correct size
   * @return
   */
  public double[] getSize() {
    return new double[] { getWidth() , getHeight() };
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
