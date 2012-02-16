/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg.parser.pathway;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.zbit.util.StringUtil;


/**
 * Corresponding to the Kegg Graphics class (see {@link http://www.genome.jp/kegg/xml/docs/})
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Graphics {
  /**
   * the label of this graphics object
   */
  private String name = "";
  /**
   * the X axis position of this graphics object
   */
  private int x=0;
  /**
   * the Y axis position of this graphics object
   */
  private int y=0;
  /**
   * the shape of this graphics object
   */
  private GraphicsType type = GraphicsType.rectangle;
  /**
   * the width of this graphics object
   */
  private int width=45;
  /**
   * the height of this graphics object
   */
  private int height=17;
  /**
   * the foreground color used by this graphics object
   */
  private String fgcolor="#000000";
  /**
   * the background color used by this graphics object
   * "=> for gene products"
   */
  private String bgcolor = "#FFFFFF"; // "#BFFFBF";
  /**
   * Multiple x,y,x,y,... coordinates for a line"
   */
  private Integer[] coords = null;
  
  /**
   * Should not be public, because isGeneProduct information is important!
   */
  private Graphics(){
    super();
  }
  
  public Graphics(String name){
    this();
    this.name = name;
  }
  
//  public Graphics(String name, GraphicsType type){
//    this();
//    this.name = name;
//    this.type = type;
//  }
  /**
   * 
   * @param isGeneProduct - (EntryType==EntryType.gene)
   */
  public Graphics( boolean isGeneProduct) {
    this();
    if (isGeneProduct) bgcolor = "#BFFFBF"; // Default for gene product
  }
  
  /**
   * Create a new graphics object, based on the given entry.
   * Does NOT set this graphics as the graphics object of e and
   * does NOT consider any other graphics object that may already
   * belong to e.
   * @param e
   */
  public Graphics(Entry e) {
    this(e.getType()==EntryType.gene || e.getType()==EntryType.genes);
  }
  
  /**
   * 
   * @param name
   * @param x
   * @param y
   * @param type
   * @param width
   * @param height
   * @param fgcolor
   * @param bgcolor
   * @param isGeneProduct - (EntryType==EntryType.gene)
   */
  public Graphics(String name, int x, int y, GraphicsType type, int width, int height, String fgcolor, String bgcolor, boolean isGeneProduct) {
    this(isGeneProduct);
    this.name = name;
    this.x = x;
    this.y = y;
    this.type = type;
    this.width = width;
    this.height = height;
    this.fgcolor = fgcolor;
    this.bgcolor = bgcolor;
  }
  
  /**
   * Returns true if and only if the background color has been set.
   * @return
   */
  public boolean isBGcolorSet() {
    if (bgcolor!=null && bgcolor.length()>0 && !bgcolor.trim().equalsIgnoreCase("none"))
      return true;
    return false;
  }
  
  /**
   * The bgcolor attribute specifies the background color of this object. The default
   * value is "#FFFFFF". The background color for the gene product is "#BFFFBF".
   * @return Background color
   */
  public String getBgcolor() {
    return bgcolor;
  }
  
  /**
   * The coords attribute specifies a set of coordinates, x1,y1,x2,y2,..., for the line object.
   * @return Integer of X,Y,X,Y,X,Y,...
   */
  public Integer[] getCoords() {
    if (coords!=null) return coords;
    else return new Integer[]{x, y, (x+width), (y+height)};
  }
  
  /**
   * The fgcolor attribute specifies the foreground color of this object. It applies
   * to the frame and the character string. The default value is "#000000".
   * @return
   */
  public String getFgcolor() {
    return fgcolor;
  }
  
  /**
   * The height attribute specifies the height of this object. The default value is "17".
   * @return
   */
  public int getHeight() {
    return height;
  }
  
  /**
   * The name attribute contains the label that is associated with this graphics object.
   * When two or more name attributes are specified in the same entry element, the first
   * one is taken as the attribute value. When the type attribute value of the entry element
   * is "gene", the gene name is specified for this attribute value. 
   * @return the label of this graphics object (e.g. "1.1.1.43" or "Methane metabolism")
   */
  public String getName() {
    return name;
  }
  
  /**
   * The type attribute specifies the shape of this object. The default value is "rectangle".
   * <table style="" border="1">
  <tbody><tr id="item"> <td id="a1">attribute value</td> <td id="a2">explanation</td> </tr>
  <tr><td>rectangle</td>
  <td>the shape is a rectangle, which is used to represent a gene product and its complex
  (including an ortholog group).</td></tr>
  <tr><td>circle</td>
  <td>the shape is a circle, which is used to specify any other molecule such as a chemical
  compound and a glycan.</td></tr>
  <tr><td>roundrectangle</td>
  <td>the shape is a round rectangle, which is used to represent a linked pathway.</td></tr>
  <tr><td>line</td>
  <td>the shape is a polyline, which is used to represent a reaction or a relation
  (and also a gene or an ortholog group).</td></tr>
  </tbody></table>
   * @return GraphicsType
   */
  public GraphicsType getType() {
    return type;
  }
  
  /**
   * The width attribute specifies the width this object. The default value is "45".
   * @return
   */
  public int getWidth() {
    return width;
  }
  
  /**
   * The x attribute specifies the x-coordinate value of this graphics object
   * in the manually drawn KEGG pathway map.
   * @return
   */
  public int getX() {
    return x;
  }
  
  /**
   * The y attribute specifies the y-coordinate value of this graphics object
   * in the manually drawn KEGG pathway map.
   * @return
   */
  public int getY() {
    return y;
  }
  
  /**
   * @param bgcolor
   */
  public void setBgcolor(String bgcolor) {
    this.bgcolor = bgcolor;
  }
  
  /**
   * 
   * @param fgcolor
   */
  public void setFgcolor(String fgcolor) {
    this.fgcolor = fgcolor;
  }
  
  /**
   * 
   * @param height
   */
  public void setHeight(int height) {
    this.height = height;
  }
  
  /**
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * 
   * @param type
   */
  public void setType(GraphicsType type) {
    this.type = type;
  }
  
  /**
   * 
   * @param width
   */
  public void setWidth(int width) {
    this.width = width;
  }
  
  /**
   * 
   * @param x
   */
  public void setX(int x) {
    this.x = x;
  }
  
  /**
   * 
   * @param y
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Set a coords string as specified by the API. Only makes sense if
   * {@link #getType()} is a "line"! Should give coordinates for this line.
   * @param coords
   */
  public void setCoordsString(String coords) {
    String[] splitt = coords.split(",");
    List<Integer> co = new LinkedList<Integer>();
    for (String s: splitt) {
      co.add(Integer.parseInt(s));
    }
    this.coords  = co.toArray(new Integer[0]);
  }

  /**
   * @return
   */
  public boolean isSetCoords() {
    // at least 2 coordinates
    return coords!=null && coords.length>1;
  }

  /**
   * Returns true if and only if the foreground color has been set.
   * @return
   */
  public boolean isFGcolorSet() {
    if (fgcolor!=null && fgcolor.length()>0 && !fgcolor.trim().equalsIgnoreCase("none"))
      return true;
    return false;
  }

  public Map<String, String> getKGMLAttributes() {
    Map<String, String> attributes = new HashMap<String, String>();

    if(isSetName()){
      attributes.put("name", name);
    }        
    if(isSetX()){
      attributes.put("x", String.valueOf(x));
    }
    if(isSetY()){
      attributes.put("y", String.valueOf(y));
    }
    if(isSetCoords()) {
      attributes.put("coords", StringUtil.implode(coords, ","));
    }
    if(isSetType()){
      attributes.put("type", type.toString());
    }
    if(isSetWidth()){
      attributes.put("width", String.valueOf(width));
    }
    if(isSetHeight()){
      attributes.put("heigth", String.valueOf(height));
    }
    if(isSetFGcolor()){
      attributes.put("fgcolor", fgcolor);
    }
    if(isSetBGcolor()){
      attributes.put("bgcolor", bgcolor);
    }
    
    return attributes;
  }

  private boolean isSetFGcolor() {    
    return fgcolor!=null;
  }
  private boolean isSetBGcolor() {    
    return bgcolor!=null;
  }

  private boolean isSetType() {
    return type!=null;
  }

  private boolean isSetHeight() {
    return height>=0;
  }

  private boolean isSetWidth() {
    return width>=0;
  }

  private boolean isSetY() {
    return y>=0;
  }

  private boolean isSetX() {    
    return x>=0;
  }

  private boolean isSetName() {
    return name!=null;
  }
  
  @Override
  public int hashCode() {
    int hash = 17;
    if(isSetName())
      hash *= name.hashCode();
    if(isSetX())
      hash *= x;
    if(isSetY())
      hash *= y;
    if(isSetCoords())
      hash *= coords.hashCode();
    if(isSetType())
      hash *= type.hashCode();
    if(isSetWidth())
      hash *= width;
    if(isSetHeight())
      hash *= height;
    if(isSetFGcolor())
      hash *= fgcolor.hashCode();
    
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = true;
    if(obj.getClass().isAssignableFrom(Graphics.class)){    
      Graphics o = (Graphics)obj;
      
      equals &= o.isSetName()==this.isSetName();
      if(equals && isSetName()) 
        equals &= (o.getName().equals(this.getName()));
      
      equals &= o.isSetX()==this.isSetX();
      if(equals && isSetX()) 
        equals &= (o.getX() == this.getX());
      
      equals &= o.isSetY()==this.isSetY();
      if(equals && isSetY()) 
        equals &= (o.getY() == this.getY());
      
      equals &= o.isSetCoords() == this.isSetCoords();
      if(equals && isSetCoords()){
        equals &= (o.getCoords().equals(coords));
      }
      
      equals &= o.isSetType()==this.isSetType();
      if(equals && isSetType()) 
        equals &= (o.getType().equals(this.getType()));
      
      equals &= o.isSetWidth()==this.isSetWidth();
      if(equals && isSetWidth()) 
        equals &= (o.getWidth()==this.getWidth());
      
      equals &= o.isSetHeight()==this.isSetHeight();
      if(equals && isSetHeight()) 
        equals &= (o.getHeight()==  this.getHeight());
      
      equals &= o.isSetFGcolor()==this.isSetFGcolor();
      if(equals && isSetFGcolor()) 
        equals &= (o.getFgcolor().equals(this.getFgcolor()));
    }
    return equals;
  }
  
}
